"""
node_elevations.csv / edge_slopes.csv 의 값을 driving_db 에 일괄 적용한다.

사용법:
    cd Taxi-Driver-BE/docs/data
    py apply_elevation_slope.py                  # dry-run (DB 변경 없음)
    py apply_elevation_slope.py --apply          # 실제 UPDATE 실행
    py apply_elevation_slope.py --apply --host=localhost --user=root --password=YOUR_PW

전제조건:
    - MySQL 서버 가동 중, driving_db 스키마 존재
    - graph_nodes / graph_edges 테이블이 이미 import 되어 있어야 함
      (이 스크립트는 elevation/slope 컬럼만 UPDATE 함, INSERT 하지 않음)
    - pip install pymysql

빈 칸은 NULL 로 처리 (한강 다리 위 노드 138개 + 그 노드를 잇는 엣지 334개).
"""
import argparse
import csv
import os
import sys

try:
    import pymysql
except ImportError:
    sys.exit("pymysql 가 필요합니다.  py -m pip install pymysql")

HERE = os.path.dirname(os.path.abspath(__file__))
NODE_CSV = os.path.join(HERE, "node_elevations.csv")
EDGE_CSV = os.path.join(HERE, "edge_slopes.csv")

ap = argparse.ArgumentParser()
ap.add_argument("--apply", action="store_true", help="실제 UPDATE 실행")
ap.add_argument("--host", default="localhost")
ap.add_argument("--port", type=int, default=3306)
ap.add_argument("--user", default="root")
ap.add_argument("--password", default="0728")
ap.add_argument("--database", default="driving_db")
args = ap.parse_args()

print(f"[mode] {'APPLY' if args.apply else 'DRY-RUN'}")
print(f"[db  ] {args.user}@{args.host}:{args.port}/{args.database}")


def load_csv(path):
    """CSV → [(id, value or None), ...] 빈 칸은 None."""
    with open(path, encoding="utf-8") as f:
        reader = csv.reader(f)
        next(reader)  # header
        for row in reader:
            yield row[0], (None if row[1] == "" else float(row[1]))


# ── 로드 ──────────────────────────────────────────────────────
node_rows = list(load_csv(NODE_CSV))
edge_rows = list(load_csv(EDGE_CSV))

n_filled = sum(1 for _, v in node_rows if v is not None)
e_filled = sum(1 for _, v in edge_rows if v is not None)
print(f"[csv ] node_elevations.csv : {len(node_rows):,} rows "
      f"({n_filled:,} filled, {len(node_rows)-n_filled} NULL)")
print(f"[csv ] edge_slopes.csv     : {len(edge_rows):,} rows "
      f"({e_filled:,} filled, {len(edge_rows)-e_filled} NULL)")

if not args.apply:
    print()
    print("DRY-RUN 종료. --apply 추가해 실제 적용.")
    sys.exit(0)

# ── DB 연결 + 사전 검증 ───────────────────────────────────────
conn = pymysql.connect(
    host=args.host, port=args.port,
    user=args.user, password=args.password,
    database=args.database, autocommit=False,
)
cur = conn.cursor()

cur.execute("SELECT COUNT(*) FROM graph_nodes")
db_nodes = cur.fetchone()[0]
cur.execute("SELECT COUNT(*) FROM graph_edges")
db_edges = cur.fetchone()[0]
print(f"[db  ] graph_nodes={db_nodes:,}  graph_edges={db_edges:,}")
if db_nodes == 0 or db_edges == 0:
    sys.exit("graph_nodes / graph_edges 가 비어 있습니다. 먼저 그래프 데이터를 import 하세요.")

# ── UPDATE ───────────────────────────────────────────────────
CHUNK = 1000

print()
print("[apply] graph_nodes.elevation UPDATE")
node_payload = [
    (None if v is None else float(v), int(nid)) for nid, v in node_rows
]
for i in range(0, len(node_payload), CHUNK):
    chunk = node_payload[i:i + CHUNK]
    cur.executemany(
        "UPDATE graph_nodes SET elevation=%s WHERE node_id=%s", chunk
    )
    print(f"   {i+len(chunk):>6,}/{len(node_payload):,}")

print()
print("[apply] graph_edges.slope UPDATE")
edge_payload = [
    (None if v is None else float(v), eid) for eid, v in edge_rows
]
for i in range(0, len(edge_payload), CHUNK):
    chunk = edge_payload[i:i + CHUNK]
    cur.executemany(
        "UPDATE graph_edges SET slope=%s WHERE edge_id=%s", chunk
    )
    print(f"   {i+len(chunk):>6,}/{len(edge_payload):,}")

conn.commit()
print()
print("[done] COMMIT 완료")

# 사후 검증
cur.execute("SELECT COUNT(*), COUNT(elevation) FROM graph_nodes")
print("graph_nodes (total, filled):", cur.fetchone())
cur.execute("SELECT COUNT(*), COUNT(slope) FROM graph_edges")
print("graph_edges (total, filled):", cur.fetchone())

conn.close()
