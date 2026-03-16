# CLAUDE.md - Backend 팀원용 프로젝트 컨텍스트

## 프로젝트 개요

**인지 중심 운전 경로 추천 서비스** - 초보 운전자를 위해 도로 위험 요소를 정량화(난이도 0~100점)하고, 개인 숙련도/불안 요소를 반영한 맞춤형 안전 경로를 제공하는 시스템.

## 아키텍처

```
React (Vite, :5173) --> Spring Boot (:8080) --> MySQL (:3306, driving_db)
                             |
                         카카오맵 API (경로/지도)
```

- Supabase 사용하지 않음. Spring Boot + MySQL 단일 구조.
- 1~7주차: mock 유저(user_id=1)로 개발, 8주차에 OAuth2+JWT 붙임.

## 팀원 역할 (Backend)

| 담당 | 역할 |
|------|------|
| **준영** | DB(MySQL) + BE + Infra - Entity 설계, 데이터 임포트, Azure 배포, CORS, 캐싱 |
| **승종** | Backend - 도로 데이터 조회 API, 경로 탐색 API, API 문서화 |
| **해석** | BE 핵심 - 설문/취약특성 API, A* 알고리즘, 난이도 가중 경로 탐색, OAuth2(8주차~) |

## 실행 방법

```bash
# 사전: MySQL에 driving_db 생성
# CREATE DATABASE driving_db;

# Windows
gradlew.bat bootRun

# Unix/Mac
./gradlew bootRun
```

application.yml에서 MySQL 비밀번호 확인:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/driving_db?useSSL=false&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true
    username: root
    password: root1234  # 본인 MySQL 비밀번호로 변경
```

## 패키지 구조 (목표)

```
com.driving.backend/
├── entity/          # JPA Entity (ERD 기반 재설계 필요)
├── repository/      # Spring Data JPA Repository
├── service/         # 비즈니스 로직
├── controller/      # REST API 엔드포인트
├── dto/             # Request/Response DTO
├── config/          # CORS, Security 등 설정
└── BackendApplication.java
```

**현재 상태 (2026-03-16)**: Entity 재설계 완료. repository/service/controller는 gitkeep만 존재, PSJ/week2 브랜치에서 구현 중.

## DB 스키마 (ERD 기반, 최신)

> 상세: `docs/DB_SCHEMA.md` (dbdiagram.io DBML 형식)

### 테이블 관계 요약

```
users (1) ──── (1) user_profile
                     └── vulnerability_type_id → vulnerability_type

road_segments (1) ── (N) segment_vulnerability_map → vulnerability_type
road_segments (N) ──── (1) level_rule
```

> ⚠️ [JY] Entity 통합 (2026-03-16): segment_score + segment_level → road_segments 단일 테이블로 통합됨
> RoadSegment Entity에 totalScore, level, levelScore, 5개 세부점수, explanation 등이 직접 포함

### 핵심 테이블

**users** - 사용자 기본 정보
```
user_id (PK, BIGINT), email (UNIQUE), nickname, created_at
```

**user_profile** - 사용자 숙련도 & 취약특성 (users와 1:1)
```
user_id (PK, FK), skill_level (INT, 0~100),
vulnerability_type_id (FK, 단일 선택), created_at, updated_at
```
> ⚠️ user_vulnerability_map N:M 테이블 없음 - 단일 FK로 통합

**vulnerability_type** - 취약 특성 코드 (5개 고정)
```
vulnerability_type_id (PK), code, name, description, icon_key
```
| ID | code | name |
|----|------|------|
| 1 | AVOID_HIGHWAY | 고속/간선도로 회피 |
| 2 | PREFER_WIDE_ROAD | 넓은 도로 선호 |
| 3 | AVOID_COMPLEX_INTERSECTION | 복잡한 교차로 회피 |
| 4 | AVOID_HIGH_TRAFFIC | 교통량 많은 도로 회피 |
| 5 | AVOID_ACCIDENT_PRONE | 사고다발구간 회피 |

**road_segments** - 도로 세그먼트 지리 정보 (7,200개)
```
segment_id (PK, "SEG_000001"), name, highway,
start_lat/lon, end_lat/lon, center_lat/lon,
num_points, coordinates_json (polyline JSON),
created_at, updated_at
```

**segment_score** - 세그먼트 난이도 점수 (road_segments와 1:1)
```
segment_id (PK, FK),
total_score (0~100), level_text,
accident_rate_score, road_shape_score, road_scale_score,
intersection_score, traffic_volume_score,
explanation (Hover용), detail_description (Click용),
detail_source, detail_source_ref, detail_updated_at,
computed_at
```

**level_rule** - 난이도 산출 규칙 (1개)
```
level_rule_id (PK), version, name,
w_accident_rate(0.25), w_road_shape(0.20), w_road_scale(0.15),
w_intersection(0.15), w_traffic_volume(0.25),
level1_max(31.0), level2_max(41.8), is_active, created_at
```

**segment_level** - 세그먼트별 레벨 산출 결과 (PK: segment_id + level_rule_id)
```
segment_id (PK, FK), level_rule_id (PK, FK),
level (1~3), level_score, computed_at
```

**segment_vulnerability_map** - 세그먼트-취약특성 매핑 (9,018개)
```
segment_id (PK, FK), vulnerability_type_id (PK, FK),
severity (0.5~1.0), note, source, updated_at
```

### Entity 현황 (재설계 완료)

| Entity | 상태 | 비고 |
|--------|------|------|
| `User` | ✅ | email, nickname |
| `UserProfile` | ✅ | skill_level, vulnerability_type_id FK |
| `RoadSegment` | ✅ | 지리정보 + score + level 통합 |
| `LevelRule` | ✅ | 난이도 산출 규칙 |
| `VulnerabilityType` | ✅ | 5개 고정 코드 |
| `SegmentVulnerabilityMap` | ✅ | 복합키(segmentId + vulnerabilityTypeId) |
| `Route`, `Feedback` | ❌ 미구현 | 6~8주차 경로 탐색 Phase에서 설계 예정 |

## 난이도 산출 공식

```
난이도 = 0.25 x 사고율 + 0.20 x 도로형태 + 0.15 x 도로규모 + 0.15 x 교차로 + 0.25 x 교통량
```

Level 판정 (상대평가):
- Level 1 (쉬움): <= 31.0점 (42.9%, 3,092개)
- Level 2 (보통): 31.1 ~ 41.8점 (37.2%, 2,675개)
- Level 3 (어려움): > 41.8점 (19.9%, 1,433개)

## API 기능 명세

> 상세: `docs/API_SPEC.csv`

### 주요 API 목록

**설문/유저 (해석 담당)**
- GET /api/survey/questions - 설문 문항 불러오기
- POST /api/survey/results - 설문 결과 저장
- GET /api/survey/results/summary - 설문 결과 요약
- GET /api/users/me/profile - 프로필 조회
- PUT /api/users/me/profile - 프로필 수정
- POST /api/survey/reset - 설문 재설정
- GET /api/users/me/vulnerabilities - 사용자 취약특성 조회

**도로 데이터 (승종 + 준영 담당)**
- GET /api/segments - 도로 세그먼트 조회 (bounds 쿼리)
- GET /api/segments/{id} - 도로 상세조회
- GET /api/segments/{id}/difficulty - 난이도 조회
- GET /api/segments/{id}/tooltip - 요약 정보 (툴팁)
- GET /api/segments/{id}/score-detail - 난이도 구성요소
- GET /api/segments/{id}/vulnerabilities - 취약특성 상세
- GET /api/vulnerabilities/{typeId}/segments - 취약특성별 위치 조회

**경로 탐색 (6~8주차, 해석 + 승종)**
- POST /api/routes/search - 경로 탐색
- GET /api/routes/{id} - 경로 조회

**인증 (8주차~, 해석)**
- POST /api/auth/login - 구글 OAuth2 로그인
- POST /api/auth/logout - 로그아웃

## 데이터 파일

> 상세: `docs/DATA_GUIDE.md`

`docs/data/` 에 전체 데이터가 포함되어 있음.

### DB 임포트용 (erd_output/, FK 순서대로)

| 순서 | 파일 | 레코드 |
|------|------|--------|
| 1 | vulnerability_type.csv | 5 |
| 2 | level_rule.csv | 1 |
| 3 | road_segments.csv | 7,200 |
| 4 | segment_score.csv | 7,200 |
| 5 | segment_level.csv | 7,200 |
| 6 | segment_vulnerability_map.csv | 9,018 |

> 주의: 현재 ERD(DB_SCHEMA.md)는 road_segments에 score/level을 통합한 구조.
> erd_output의 CSV는 분리된 구조(구 ERD 기준)이므로, Entity 설계 시 통합하여 임포트하거나
> 기존 분리 구조 그대로 사용할지 팀 내 결정 필요.

### 원천 데이터 (참고용)

- `raw/gangnam_roads_raw.csv` - OSM 도로 6,498개
- `raw/gangnam_intersections.csv` - 교차로/신호등 1,350개
- `accident/accident_locations_geocoded.csv` - 사고 다발 지점 40개
- `processed/gangnam_road_difficulty.csv` - 난이도 산정 결과 7,200개

## 개발 일정

> 상세: `docs/DEV_PLAN.md`

**완료 (Phase 1, 1~2주차):**
- ✅ ERD 기반 Entity 재설계 (준영 + 승종)
- ✅ 패키지 구조 셋업
- ✅ [JY] SegmentScore/Level → RoadSegment 통합

**진행 중 (Phase 2, 3~4주차, PSJ/week2 브랜치):**
1. 🔄 RoadSegmentRepository 구현
2. 🔄 DTO 5개 (SegmentSummaryResponse, SegmentDetailResponse, SegmentTooltipResponse, SegmentDifficultyResponse, SegmentScoreDetailResponse)
3. 🔄 RoadSegmentService 구현
4. ⬜ RoadSegmentController 구현 (Service 완료 후)

## 주의사항

- `application.yml`의 MySQL 비밀번호는 각자 환경에 맞게 수정 (커밋하지 말 것)
- `ddl-auto: update` 설정이므로 Entity 변경 시 테이블이 자동 변경됨
- `road_segments.name`이 "이름없음"인 건 2,723개(37.8%) - NULL 변환 권장
- `segment_score.level_text`(절대평가) vs `segment_level.level`(상대평가) 불일치 있음 → `level` 사용 권장
- Frontend는 별도 레포 (FE 담당: 민규)
