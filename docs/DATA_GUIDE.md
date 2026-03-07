# ERD 출력 데이터 가이드 (Backend Import용)

> 작성일: 2026-02-16
> 생성 스크립트: `라벨링/convert_to_erd.py`
> 원천 데이터: `라벨링/data/processed/gangnam_road_difficulty.csv`, `gangnam_road_segments.csv`
> ERD Diagram: https://dbdiagram.io/d/697aed26bd82f5fce2f891dd

---

## 1. 파일 목록 및 Import 순서

FK 제약조건 때문에 **반드시 아래 순서대로** import해야 합니다.

| 순서 | 파일명 | 대응 테이블 | 레코드 수 | 설명 |
|------|--------|-------------|-----------|------|
| 1 | `vulnerability_type.csv` | vulnerability_type | 5 | 취약 특성 메타데이터 (코드 테이블) |
| 2 | `level_rule.csv` | level_rule | 1 | Level 산출 규칙 정의 |
| 3 | `road_segments.csv` | road_segments | 7,200 | 도로 세그먼트 지리 정보 |
| 4 | `segment_score.csv` | segment_score | 7,200 | 세그먼트별 원천 점수 및 설명 |
| 5 | `segment_level.csv` | segment_level | 7,200 | 세그먼트별 Level(1~3) 결과 |
| 6 | `segment_vulnerability_map.csv` | segment_vulnerability_map | 9,018 | 세그먼트-취약특성 매핑 |

> **참고**: `users`, `user_profile` 테이블은 런타임에 사용자가 생성하는 데이터이므로 사전 적재 대상이 아닙니다.

### FK 의존관계도

```
vulnerability_type (참조 없음)
level_rule         (참조 없음)
road_segments      (참조 없음)
    ↑
    ├── segment_score                → road_segments.segment_id (1:1)
    ├── segment_level                → road_segments.segment_id + level_rule.level_rule_id
    └── segment_vulnerability_map    → road_segments.segment_id + vulnerability_type.vulnerability_type_id
```

---

## 2. 테이블별 상세 명세

### 2-1. vulnerability_type (5건)

취약 특성의 종류를 정의하는 **코드 테이블**입니다. 사용자가 설문 시 선택하는 항목이며, 경로 추천 시 해당 특성을 가진 세그먼트에 가중 페널티를 부여합니다.

| 컬럼명 | 타입 | PK/FK | NOT NULL | 설명 |
|--------|------|-------|----------|------|
| vulnerability_type_id | int | PK | O | 취약 특성 ID (1~5) |
| code | varchar(50) | UNIQUE | O | 코드값 (예: `AVOID_HIGHWAY`) |
| name | varchar(100) | | O | 한글 표시명 (예: "고속/간선도로 회피") |
| description | text | | | 클릭 시 상세 설명 |
| icon_key | varchar(50) | | | 프론트엔드 아이콘 키 |

**데이터 내용:**

| ID | code | name |
|----|------|------|
| 1 | AVOID_HIGHWAY | 고속/간선도로 회피 |
| 2 | PREFER_WIDE_ROAD | 넓은 도로 선호 |
| 3 | AVOID_COMPLEX_INTERSECTION | 복잡한 교차로 회피 |
| 4 | AVOID_HIGH_TRAFFIC | 교통량 많은 도로 회피 |
| 5 | AVOID_ACCIDENT_PRONE | 사고다발구간 회피 |

---

### 2-2. level_rule (1건)

난이도 Level(1~3)을 산출하는 **규칙 정의** 테이블입니다. 향후 규칙 변경 시 새로운 row를 추가하고 `is_active`를 갱신하여 버전 관리합니다.

| 컬럼명 | 타입 | PK/FK | NOT NULL | 설명 |
|--------|------|-------|----------|------|
| level_rule_id | bigint | PK | O | 규칙 ID (auto increment) |
| version | varchar(30) | UNIQUE | O | 규칙 버전 (현재: `v1.0`) |
| name | varchar(80) | | O | 규칙 이름 |
| w_accident_rate | double | | O | 사고율 가중치 |
| w_road_shape | double | | O | 도로형태 가중치 |
| w_road_scale | double | | O | 도로규모 가중치 |
| w_intersection | double | | O | 교차로 가중치 |
| w_traffic_volume | double | | O | 교통량 가중치 |
| level1_max | double | | O | Level 1 상한 점수 |
| level2_max | double | | O | Level 2 상한 점수 |
| is_active | boolean | | O | 현재 운영 중인 규칙 여부 |
| created_at | datetime | | O | 규칙 생성 시각 |

**현재 규칙 (v1.0):**

```
가중합 = 0.25×사고율 + 0.20×도로형태 + 0.15×도로규모 + 0.15×교차로 + 0.25×교통량

Level 판정 (상대평가 백분위):
  Level 1 (쉬움)  : 가중합 ≤ 31.0  (하위 30%)
  Level 2 (보통)  : 31.0 < 가중합 ≤ 41.8  (중위 50%)
  Level 3 (어려움): 가중합 > 41.8  (상위 20%)
```

---

### 2-3. road_segments (7,200건)

강남구 도로를 500m 단위로 분할한 **세그먼트 지리 정보**입니다. 지도 렌더링의 기본 단위이며, 다른 테이블들이 `segment_id`로 참조합니다.

| 컬럼명 | 타입 | PK/FK | NOT NULL | 설명 |
|--------|------|-------|----------|------|
| segment_id | varchar(50) | PK | O | 세그먼트 ID (형식: `SEG_000001`) |
| name | varchar(120) | | | 도로명 (NULL 가능, 37.8%가 "이름없음") |
| highway | varchar(30) | | | OSM 도로 유형 |
| start_lat | double | | O | 시작점 위도 |
| start_lon | double | | O | 시작점 경도 |
| end_lat | double | | O | 끝점 위도 |
| end_lon | double | | O | 끝점 경도 |
| center_lat | double | | | 중심점 위도 |
| center_lon | double | | | 중심점 경도 |
| num_points | int | | | polyline 좌표 개수 |
| coordinates_json | json | | O | polyline 좌표 배열 (지도 라인 렌더링용) |
| created_at | datetime | | O | 적재 시각 |
| updated_at | datetime | | | 갱신 시각 (초기 적재 시 NULL) |

**highway 유형별 분포:**

| highway | 건수 | 설명 |
|---------|------|------|
| residential | 2,877 | 주거지역 도로 |
| service | 2,617 | 서비스 도로 (주차장 등) |
| primary | 503 | 주간선도로 |
| secondary | 302 | 보조간선도로 |
| trunk_link | 292 | 간선도로 연결로 |
| tertiary | 259 | 집산도로 |
| trunk | 141 | 도시고속도로급 간선도로 |
| primary_link | 101 | 주간선도로 연결로 |
| secondary_link | 64 | 보조간선도로 연결로 |
| tertiary_link | 22 | 집산도로 연결로 |
| unclassified | 22 | 미분류 도로 |

**coordinates_json 형식 예시:**

```json
[[127.0304396, 37.5060425], [127.0296182, 37.5051497], [127.0286256, 37.503966]]
```

> 배열 내 각 원소는 `[경도(lon), 위도(lat)]` 순서입니다.

---

### 2-4. segment_score (7,200건)

각 세그먼트의 **원천 난이도 점수와 설명**입니다. road_segments와 1:1 관계입니다.

| 컬럼명 | 타입 | PK/FK | NOT NULL | 설명 |
|--------|------|-------|----------|------|
| segment_id | varchar(50) | PK, FK→road_segments | O | 세그먼트 ID |
| total_score | double | | O | 최종 난이도 점수 (0~100) |
| level_text | varchar(20) | | | 원천 등급 텍스트 (쉬움/보통/어려움) |
| accident_rate_score | double | | O | 사고율 점수 (0~100) |
| road_shape_score | double | | O | 도로형태 점수 (0~100) |
| road_scale_score | double | | O | 도로규모 점수 (0~100) |
| intersection_score | double | | O | 교차로 점수 (0~100) |
| traffic_volume_score | double | | O | 교통량 점수 (0~100) |
| explanation | varchar(300) | | | Hover용 요약 (예: "주거지역 도로, 좁은 도로") |
| detail_description | text | | | Click용 상세 설명 (자동 생성) |
| detail_source | varchar(50) | | | 설명 출처 유형 (현재: `LABELING_SCRIPT`) |
| detail_source_ref | varchar(200) | | | 출처 참조값 (현재: `labeling_difficulty.py`) |
| detail_updated_at | datetime | | | 설명 마지막 갱신 시각 |
| computed_at | datetime | | O | 점수 계산 시각 |

**점수 통계:**

| 항목 | 값 |
|------|-----|
| total_score 범위 | 27.5 ~ 72.5 |
| total_score 평균 | 36.4 |
| total_score 중앙값 | 33.0 |
| accident_rate_score 범위 | 0.0 ~ 100.0 |
| road_shape_score 범위 | 40 ~ 70 |
| road_scale_score 범위 | 20 ~ 80 |
| intersection_score 범위 | 0 ~ 100 |
| traffic_volume_score 범위 | 20 ~ 80 |

**detail_description 예시:**

```
이 구간은 주거지역 도로입니다. 도로 폭이 좁아 차선 변경이나 회피가 어려울 수
있습니다(규모점수 70점). 주변에 신호등이나 횡단보도가 많아 교차로가 복잡합니다
(교차로점수 100점). 교통량이 적어 여유롭게 주행할 수 있습니다(교통량점수 25점).
```

**주의: `level_text` 필드에 대해**

`level_text`는 절대평가 기준(≤30 쉬움, ≤60 보통, >60 어려움)으로 생성되었습니다. `segment_level.level`은 상대평가 기준(백분위)으로 산출되므로, 두 값이 일치하지 않는 경우가 있습니다(2,518건/35%). **실제 서비스에서는 `segment_level.level` 값을 사용하는 것을 권장**합니다.

---

### 2-5. segment_level (7,200건)

각 세그먼트에 대한 **최종 Level(1~3) 산출 결과**입니다. `level_rule`의 규칙을 적용한 결과이며, 복합PK(`segment_id` + `level_rule_id`)를 사용합니다.

| 컬럼명 | 타입 | PK/FK | NOT NULL | 설명 |
|--------|------|-------|----------|------|
| segment_id | varchar(50) | PK, FK→road_segments | O | 세그먼트 ID |
| level_rule_id | bigint | PK, FK→level_rule | O | 적용된 규칙 ID (현재: 1) |
| level | int | | O | Level 1(쉬움), 2(보통), 3(어려움) |
| level_score | double | | | 가중합 점수 (디버깅/설명용) |
| computed_at | datetime | | O | 계산 시각 |

**Level 분포:**

| Level | 의미 | 건수 | 비율 | 점수 범위 |
|-------|------|------|------|-----------|
| 1 | 쉬움 | 3,092 | 42.9% | ≤ 31.0 |
| 2 | 보통 | 2,675 | 37.2% | 31.1 ~ 41.8 |
| 3 | 어려움 | 1,433 | 19.9% | > 41.8 |

---

### 2-6. segment_vulnerability_map (9,018건)

세그먼트가 어떤 취약 특성에 해당하는지를 매핑하는 **오버레이 테이블**입니다. 하나의 세그먼트가 여러 취약 특성을 가질 수 있습니다(N:M). 사용자의 취약 특성 선택에 따라 경로 추천 시 해당 세그먼트에 페널티/회피 가중치를 부여하는 데 사용됩니다.

| 컬럼명 | 타입 | PK/FK | NOT NULL | 설명 |
|--------|------|-------|----------|------|
| segment_id | varchar(50) | PK, FK→road_segments | O | 세그먼트 ID |
| vulnerability_type_id | int | PK, FK→vulnerability_type | O | 취약 특성 ID |
| severity | double | | | 취약 강도 (0.5~1.0, 높을수록 강함) |
| note | varchar(200) | | | 짧은 설명 |
| source | varchar(100) | | | 생성 출처 (현재: `RULE_BASED`) |
| updated_at | datetime | | | 갱신 시각 |

**취약특성별 매핑 건수:**

| vulnerability_type_id | code | 매핑 기준 | 건수 |
|----------------------|------|-----------|------|
| 1 | AVOID_HIGHWAY | highway IN (trunk, trunk_link) | 433 |
| 2 | PREFER_WIDE_ROAD | 도로규모점수 ≥ 70 | 5,494 |
| 3 | AVOID_COMPLEX_INTERSECTION | 교차로점수 ≥ 50 | 1,587 |
| 4 | AVOID_HIGH_TRAFFIC | 교통량점수 ≥ 60 | 1,037 |
| 5 | AVOID_ACCIDENT_PRONE | 사고율점수 ≥ 50 | 467 |

**사용 예시:**

사용자가 "넓은 도로 선호(PREFER_WIDE_ROAD)"를 선택한 경우 → `vulnerability_type_id = 2`로 매핑된 5,494개 세그먼트에 `severity` 만큼의 페널티를 부여하여 경로 탐색 시 해당 좁은 도로를 회피합니다.

---

## 3. 테이블 간 관계 요약

```
users (1) ─── (1) user_profile ───> vulnerability_type
                                          │
road_segments (1) ─── (1) segment_score   │
      │                                   │
      ├── (1:N) segment_level ───> level_rule
      │
      └── (N:M) segment_vulnerability_map ─┘
```

---

## 4. 난이도 산출 공식

```
난이도 = 0.25 × 사고율점수
       + 0.20 × 도로형태점수
       + 0.15 × 도로규모점수
       + 0.15 × 교차로점수
       + 0.25 × 교통량점수
```

각 세부 점수는 0~100 범위이며, 가중합 결과도 0~100 범위입니다.

**세부 점수 산정 기준:**

| 요소 | 가중치 | 산정 방식 |
|------|--------|-----------|
| 사고율 | 25% | 반경 300m 내 사고 다발 지점 수 + 통합지수 |
| 도로형태 | 20% | OSM highway 유형별 초보자 난이도 |
| 도로규모 | 15% | 도로 폭 추정 (좁을수록 고점수) |
| 교차로 | 15% | 반경 150m 내 신호등×25 + 횡단보도×10 |
| 교통량 | 25% | 도로 등급 기반 교통량 추정치 |

---

## 5. 알려진 제한사항

1. **`name` = "이름없음"**: 2,723건(37.8%)의 세그먼트가 도로명 없음. OSM에서 name 태그가 없는 도로(service, link 등). DB import 시 NULL로 변환하는 것을 권장합니다.

2. **`level_text` vs `level` 불일치**: `segment_score.level_text`(절대평가)와 `segment_level.level`(상대평가)의 기준이 다릅니다. 서비스에서는 `segment_level.level` 사용을 권장합니다.

3. **경사도 미반영**: DEM 데이터가 없어 경사도는 점수에 포함되지 않았습니다.

4. **OSM lanes/width 보유율 낮음**: lanes 7.5%, width 0.1% → 도로 유형(highway)으로 추정하여 점수를 산정했습니다.

5. **좌표 범위**: 1건(SEG_000166, 올림픽대로)이 강남구 bounding box 밖(경도 127.004)에 위치합니다. 도로가 경계를 넘어가는 경우입니다.

---

## 6. CSV 인코딩 및 형식

- **인코딩**: UTF-8 with BOM (`utf-8-sig`)
- **구분자**: 콤마(`,`)
- **줄바꿈**: LF
- **NULL 표현**: 빈 문자열 (CSV 내 `,,`)
- **JSON 필드**: `coordinates_json`은 CSV 셀 내에 JSON 배열 문자열로 저장. 큰따옴표로 감싸져 있음

---

## 7. JSON 파일

각 CSV와 동일한 이름의 `.json` 파일이 함께 제공됩니다. API 테스트나 프론트엔드 직접 참조용으로 사용할 수 있습니다.

```
road_segments.json
segment_score.json
level_rule.json
segment_level.json
vulnerability_type.json
segment_vulnerability_map.json
```

JSON 파일은 배열 형식(`[{...}, {...}, ...]`)이며, UTF-8(BOM 없음) 인코딩입니다.
