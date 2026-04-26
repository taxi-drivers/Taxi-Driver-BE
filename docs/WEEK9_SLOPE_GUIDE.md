# Week 9 — 도로 경사도(slope) 데이터 가이드

> 한 줄 요약: 강남구 도로망 그래프(노드 8,938 / 엣지 23,471)에 **고도(elevation)** 와 **경사도(slope)** 데이터를 부여했다.
> A* 라우팅 가중치에 반영되어 가파른 도로를 회피하는 데 사용된다.

---

## 1. 무엇이 추가되었나

### 1.1 DB 스키마 변경

| 테이블 | 컬럼 | 타입 | 의미 |
|---|---|---|---|
| `graph_nodes` | `elevation` | DOUBLE NULL | 노드 고도(m). 5m DEM 보간 결과 |
| `graph_edges` | `slope` | DOUBLE NULL | signed gradient = `(toElev − fromElev) / lengthM` (fraction). 예: `0.05` = 진행방향 +5% 오르막 |

> Entity 에는 이미 컬럼이 정의되어 있다 ([GraphNode.java](../src/main/java/com/driving/backend/entity/GraphNode.java), [GraphEdge.java](../src/main/java/com/driving/backend/entity/GraphEdge.java)). `ddl-auto: update` 설정이라 BE 만 띄워도 컬럼은 자동 추가된다 — 단, **값은 비어 있다**. 값을 채우는 게 이 가이드의 핵심.

### 1.2 신규 코드 (어제, 2026-04-26)

| 파일 | 역할 |
|---|---|
| [service/ElevationService.java](../src/main/java/com/driving/backend/service/ElevationService.java) | OpenTopoData SRTM 30m API 로 노드 elevation 일괄 조회 (legacy, 폴백 옵션) |
| [service/SlopeService.java](../src/main/java/com/driving/backend/service/SlopeService.java) | 엣지 slope 계산 후 saveAll |
| [controller/SlopeAdminController.java](../src/main/java/com/driving/backend/controller/SlopeAdminController.java) | admin 엔드포인트 3종: `/api/admin/slope/elevations`, `/compute`, `/build-all` |
| [service/GraphService.java](../src/main/java/com/driving/backend/service/GraphService.java) | `getSlopeMultiplier()` 추가 — slope 가중치 반영 (±15% clamp, 부호 무관 페널티) |

### 1.3 데이터 정밀도 업그레이드 (오늘, 2026-04-27)

외부 SRTM 30m API → **자체 구축 5m DEM** 으로 elevation/slope 재계산.

| | SRTM 30m (어제) | 5m DEM (지금) |
|---|---|---|
| 해상도 | 30m, 정수 m | **5m, float32** (6배) |
| 외부 의존 | 인터넷/API | 없음 (로컬) |
| avg \|slope\| | 4.94% | **2.54%** |
| \|slope\|>10% | 12% | 4.0% |
| 짧은 엣지 ±100% 노이즈 | 176개 | 30개 |

---

## 2. 팀원이 자기 DB 에 적용하는 방법 ⭐

> **이 섹션이 핵심.** Pull 받고 3분 안에 끝남. Python 만 있으면 됨.

### 사전 조건

1. MySQL 가동 중, `driving_db` 스키마 존재
2. `graph_nodes` / `graph_edges` 테이블이 import 되어 있어야 함 (준영님이 작업한 그래프 데이터)
3. `pip install pymysql`

### 절차

```bash
cd Taxi-Driver-BE/docs/data

# 1) 먼저 dry-run 으로 CSV 가 잘 읽히는지 확인 (DB 변경 없음)
py apply_elevation_slope.py

# 2) 문제 없으면 실제 적용
py apply_elevation_slope.py --apply
```

DB 비밀번호가 `0728` 이 아니면:

```bash
py apply_elevation_slope.py --apply --password=YOUR_PW
```

### 적용 후 결과 확인

```sql
SELECT COUNT(*) AS total, COUNT(elevation) AS filled FROM graph_nodes;
-- 기대값: 8938 / 8800

SELECT COUNT(*) AS total, COUNT(slope) AS filled FROM graph_edges;
-- 기대값: 23471 / 23137
```

> 138개 노드 / 334개 엣지가 NULL 인 건 **정상**. 한강 위 다리(청담대교/영동대교/올림픽대교) 일대 노드라 5m DEM 범위 밖이다. `GraphService.getSlopeMultiplier()` 가 NULL → 1.0배수(평탄)로 처리하므로 라우팅 영향 없음.

### 만약 BE 가 이미 가동 중이라면

elevation/slope 는 BE 시작 시점에 메모리에 그래프로 로드되므로, UPDATE 후 **BE 재시작** 필요.

```bash
./gradlew bootRun  # 또는 IDE 에서 재시작
```

---

## 3. 데이터 파일

| 파일 | 행 수 | 크기 | 비고 |
|---|---|---|---|
| [data/node_elevations.csv](data/node_elevations.csv) | 8,938 | 175KB | `node_id,elevation` |
| [data/edge_slopes.csv](data/edge_slopes.csv) | 23,471 | 513KB | `edge_id,slope` |
| [data/apply_elevation_slope.py](data/apply_elevation_slope.py) | — | 4KB | CSV → DB 일괄 UPDATE |

빈 칸은 NULL. CSV 는 UTF-8.

---

## 4. 어떻게 만들어졌나 (재현 / 정밀도 향상하려는 사람용)

> 일반 팀원은 **2번 절차만 따라하면 됨**. 이 섹션은 5m DEM 을 새로 만들거나 다른 지역에 확장하려는 사람을 위한 것.

### 4.1 데이터 소스: 1:5,000 수치지형도

- 출처: 국토지리정보원 국토정보플랫폼 (map.ngii.go.kr)
- 강남구 전역 = 도엽 15개 (각 zip 안에 .shp 다수)
- 사용 레이어:
  - `N3L_F0010000.shp` — 등고선 (Line)
  - `N3P_F0020000.shp` — 표고점 (Point)
- 좌표계: **EPSG:5186** (Korea 2000 Central Belt 2010, 중부원점 TM)

### 4.2 파이프라인

```
[ 도엽 zip 15개 ]
    │  merge_shp.py — 일괄 압축해제 + 등고선/표고점 shp 추출 + EPSG:5186 통일 + cp949
    ▼
[ Gangnam_Contours_Merged.shp ]    9,913 피처 (등고선)
[ Gangnam_Elevations_Merged.shp ]  8,121 피처 (표고점)
    │  QGIS — TIN 보간 (Triangulated Irregular Network)
    │     · 픽셀 크기 5m
    │     · z value: 등고선의 등고수치 / 표고점의 수치
    ▼
[ Gangnam_5m_DEM.tif ]             2213×2221 px, float32, EPSG:5186
    │  update_elevation_from_dem.py
    │     · graph_nodes 좌표 WGS84 → EPSG:5186 변환
    │     · rasterio.sample() 로 elevation 추출
    │     · slope = (toElev − fromElev) / lengthM 계산
    │     · MySQL 일괄 UPDATE
    ▼
[ driving_db.graph_nodes.elevation ]
[ driving_db.graph_edges.slope ]
```

### 4.3 일회성 스크립트 위치

본 작업에 사용한 스크립트는 BE 외부(작업용 Desktop 폴더)에 있다. 재현이 필요하면 거기서 가져와 쓰면 된다.

| 스크립트 | 역할 |
|---|---|
| `merge_shp.py` | 도엽 zip → 등고선/표고점 단일 shp 2개로 merge |
| `update_elevation_from_dem.py` | DEM → graph_nodes/edges 직접 UPDATE (dry-run / `--apply`) |
| `check_nodata_nodes.py` | DEM 외부에 떨어진 노드 위치 분석 |
| `verify_slope.py` | DB 의 slope 분포 / outlier 점검 |
| `export_elevation_slope_csv.py` | DB → 본 가이드의 CSV 2개 export |

---

## 5. 라우팅 가중치 반영

[GraphService.getSlopeMultiplier()](../src/main/java/com/driving/backend/service/GraphService.java) 에서 처리:

```java
private double getSlopeMultiplier(Double slope) {
    if (slope == null) return 1.0;          // NULL → 평탄 취급
    final double SLOPE_CLAMP = 0.15;        // ±15% 로 clamp
    final double SLOPE_COEFFICIENT = 3.0;
    double clamped = Math.min(Math.abs(slope), SLOPE_CLAMP);
    return 1.0 + SLOPE_COEFFICIENT * clamped;
}
```

| slope | multiplier |
|---|---|
| 0% (평탄) / NULL | 1.00× |
| 5% | 1.15× |
| 10% | 1.30× |
| 15% 이상 | 1.45× (clamp) |

**부호 무관**: 오르막/내리막 둘 다 초보 운전자에게 부담 (가속/제동 모두 어려움).

`distance-only` / `difficulty` 두 라우팅 모드 모두에 적용된다.

---

## 6. 알려진 이슈

| 이슈 | 영향 | 대처 |
|---|---|---|
| 한강 다리 위 노드 138개 NULL | 라우팅 가중치는 평탄(1.0×) 취급 — 다리는 실제로 평탄하므로 OK | 그대로 둠 |
| 짧은 엣지(<50m)에서 \|slope\|>30% 노이즈 39개 | 도로변 옹벽/고가/절벽이 DEM에 잡혀 발생 | `getSlopeMultiplier()` 의 ±15% clamp 로 차단 |
| 1:5,000 수치지형도 도엽이 강남보다 약간 넓음 (11×11km) | DEM bbox 가 강남 외곽까지 포함 | 그대로 둠 — 가장자리 엣지 보간 정확도에 오히려 유리 |

---

## 7. 변경 이력

- **2026-04-26 (어제)**: SRTM 30m DEM 으로 elevation/slope 1차 채움. BE 코드(Service/Controller/GraphService) 작성.
- **2026-04-27 (오늘)**: 자체 5m DEM 으로 elevation/slope 재계산 후 덮어쓰기. 정밀도 향상. CSV export + 본 가이드 작성.

자세한 파일별 변경은 [CHANGELOG.md](../../CHANGELOG.md) 참고.
