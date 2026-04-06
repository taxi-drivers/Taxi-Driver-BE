# Implemented API Spec

기준 브랜치: `haeseok/week5`

기본 URL 예시: `http://localhost:8080`

인증 방식:
- 로그인 후 받은 `access_token`을 헤더에 담아 전송
- `Authorization: Bearer <access_token>`

공통 에러 응답:

```json
{
  "status": 400,
  "message": "Invalid request body"
}
```

## 1. 인증

### 1.1 로그인
- Method: `POST`
- Path: `/auth/login`
- Auth: 없음

Request

```json
{
  "email": "test@test.com",
  "password": "1234"
}
```

Response `200 OK`

```json
{
  "user_id": 1,
  "nickname": "테스트유저",
  "skill_level": 50,
  "primary_vulnerability_type_id": 1,
  "access_token": "eyJ...",
  "refresh_token": "eyJ..."
}
```

### 1.2 로그아웃
- Method: `POST`
- Path: `/auth/logout`
- Auth: 필요

Response `200 OK`

```json
{
  "message": "Logged out successfully"
}
```

## 2. 설문

### 2.1 설문 문항 조회
- Method: `GET`
- Path: `/survey/questions`
- Auth: 없음

Response `200 OK`

```json
{
  "survey_version": "survey-v2",
  "questions": [
    {
      "code": "ROAD_FORM_HIGHWAY_STRESS",
      "category": "도로형태",
      "prompt": "고속도로나 자동차 전용도로 주행 시 심리적 부담을 많이 느낀다.",
      "reverse_scored": false,
      "options": [
        { "value": 1, "label": "전혀 아니다" },
        { "value": 2, "label": "그렇지 않다" },
        { "value": 3, "label": "보통이다" },
        { "value": 4, "label": "그렇다" },
        { "value": 5, "label": "매우 그렇다" }
      ]
    }
  ]
}
```

### 2.2 설문 결과 저장
- Method: `POST`
- Path: `/users/me/survey`
- Auth: 필요

Request 방식 A: 설문 답안 기반

```json
{
  "answers": {
    "ROAD_FORM_HIGHWAY_STRESS": 4,
    "ROAD_SCALE_PREFER_WIDE_ROAD": 5,
    "INTERSECTION_COMPLEX_DIFFICULTY": 3,
    "DRIVING_ENV_BACKSTREET_STRESS": 4,
    "DRIVING_HABIT_HARSH_BRAKING": 2,
    "WEATHER_ICY_CONFIDENCE": 1,
    "PEDESTRIAN_SAFETY_AVOID_BUSY_ZONE": 3,
    "PSYCHOLOGY_UNFAMILIAR_ROUTE_ANXIETY": 4,
    "DRIVING_SKILL_PARKING_CONFIDENCE": 5,
    "INCIDENT_RESPONSE_SLOW": 3
  },
  "client_version": "web-1.0.0"
}
```

Request 방식 B: 계산 결과 직접 저장

```json
{
  "skill_level": 42,
  "vulnerability_type_ids": [1, 3],
  "primary_vulnerability_type_id": 1,
  "client_version": "web-1.0.0"
}
```

Response `200 OK`

```json
{
  "user_id": 1,
  "skill_level": 42,
  "vulnerability_type_ids": [1, 3],
  "primary_vulnerability_type_id": 1,
  "updated_at": "2026-04-06T19:12:00"
}
```

### 2.3 설문 결과 요약 조회
- Method: `GET`
- Path: `/users/me/survey`
- Auth: 필요

Response `200 OK`

```json
{
  "user_id": 1,
  "email": "test@test.com",
  "nickname": "테스트유저",
  "skill_level": 42,
  "primary_vulnerability_type_id": 1,
  "vulnerability_type_ids": [1, 3],
  "vulnerability_types": [
    {
      "vulnerability_type_id": 1,
      "code": "AVOID_HIGHWAY",
      "name": "고속도로 회피",
      "description": "고속/전용도로 주행 부담",
      "icon_key": "highway"
    }
  ],
  "created_at": "2026-04-06T18:30:00",
  "updated_at": "2026-04-06T19:12:00"
}
```

## 3. 사용자 프로필

### 3.1 내 프로필 조회
- Method: `GET`
- Path: `/users/me`
- Auth: 필요

동일 응답:
- `GET /users/me/profile`
- `GET /users/me/vulnerabilities`

Response `200 OK`

```json
{
  "user_id": 1,
  "email": "test@test.com",
  "nickname": "테스트유저",
  "skill_level": 42,
  "primary_vulnerability_type_id": 1,
  "vulnerability_type_ids": [1, 3],
  "vulnerability_types": [
    {
      "vulnerability_type_id": 1,
      "code": "AVOID_HIGHWAY",
      "name": "고속도로 회피",
      "description": "고속/전용도로 주행 부담",
      "icon_key": "highway"
    }
  ],
  "created_at": "2026-04-06T18:30:00",
  "updated_at": "2026-04-06T19:12:00"
}
```

### 3.2 내 프로필 수정
- Method: `PATCH`
- Path: `/users/me/profile`
- Auth: 필요

Request

```json
{
  "nickname": "초보운전자"
}
```

Response `200 OK`

```json
{
  "user_id": 1,
  "email": "test@test.com",
  "nickname": "초보운전자",
  "updated_at": "2026-04-06T19:20:00"
}
```

## 4. 지도

### 4.1 지도 초기화
- Method: `GET`
- Path: `/map/init`
- Auth: 필요

Query Params
- `minLat` number
- `minLon` number
- `maxLat` number
- `maxLon` number
- `includeSegments` boolean, optional
- `levels` string, optional. 예: `1,2,3`

Example
- `/map/init?minLat=37.49&minLon=127.02&maxLat=37.52&maxLon=127.06&includeSegments=true&levels=1,2,3`

Response `200 OK`

```json
{
  "user": {
    "user_id": 1,
    "nickname": "초보운전자",
    "skill_level": 42,
    "primary_vulnerability_type_id": 1
  },
  "user_vulnerability_type_ids": [1, 3],
  "level_rule": {
    "level_rule_id": 1,
    "version": "v1",
    "name": "기본 룰",
    "level1_max": 21.0,
    "level2_max": 41.8
  },
  "color_scheme": {
    "1": "#2ECC71",
    "2": "#F1C40F",
    "3": "#E74C3C"
  },
  "vulnerability_types": [
    {
      "vulnerability_type_id": 1,
      "code": "AVOID_HIGHWAY",
      "name": "고속도로 회피",
      "description": "고속/전용도로 주행 부담",
      "icon_key": "highway"
    }
  ],
  "segments": [
    {
      "segment_id": "SEG_000001",
      "name": "테헤란로",
      "highway": "primary",
      "center": { "lat": 37.501, "lon": 127.039 },
      "coordinates": [
        { "lat": 37.5009, "lon": 127.0388 },
        { "lat": 37.5011, "lon": 127.0392 }
      ],
      "level": 2,
      "level_text": "보통",
      "total_score": 34.8,
      "explanation": "차선 수와 교차로 복잡도가 반영됨",
      "computed_at": "2026-03-21T22:00:00"
    }
  ]
}
```

### 4.2 지도 세그먼트 조회
- Method: `GET`
- Path: `/map/segments`
- Auth: 없음

Query Params
- `minLat`, `minLon`, `maxLat`, `maxLon` 필수
- `levels` optional
- `simplified` optional
- `computedAfter` optional, ISO datetime

Response `200 OK`

```json
{
  "count": 1,
  "items": [
    {
      "segment_id": "SEG_000001",
      "name": "테헤란로",
      "highway": "primary",
      "center": { "lat": 37.501, "lon": 127.039 },
      "coordinates": [
        { "lat": 37.5009, "lon": 127.0388 },
        { "lat": 37.5011, "lon": 127.0392 }
      ],
      "level": 2,
      "level_text": "보통",
      "total_score": 34.8,
      "explanation": "차선 수와 교차로 복잡도가 반영됨",
      "computed_at": "2026-03-21T22:00:00"
    }
  ]
}
```

### 4.3 사용자 취약 세그먼트 조회
- Method: `GET`
- Path: `/map/segments/vulnerable`
- Auth: 필요

Query Params
- `minLat`, `minLon`, `maxLat`, `maxLon` 필수
- `minSeverity` optional
- `returnMode` optional. `SEGMENT`이면 좌표 포함
- `levels` optional

Response `200 OK`

```json
{
  "user_id": 1,
  "vulnerability_type_ids": [1, 3],
  "count": 1,
  "items": [
    {
      "segment_id": "SEG_000001",
      "vulnerability_type_id": 1,
      "severity": 0.8,
      "note": "고속 주행 부담 가능",
      "source": "rule-based",
      "level": 2,
      "level_text": "보통",
      "total_score": 34.8,
      "explanation": "차선 수와 교차로 복잡도가 반영됨",
      "center": { "lat": 37.501, "lon": 127.039 },
      "coordinates": [
        { "lat": 37.5009, "lon": 127.0388 },
        { "lat": 37.5011, "lon": 127.0392 }
      ]
    }
  ]
}
```

### 4.4 지도 상세 패널 조회
- Method: `GET`
- Path: `/map/segments/{segment_id}/detail`
- Auth: 없음

Response `200 OK`

```json
{
  "segment_id": "SEG_000001",
  "name": "테헤란로",
  "highway": "primary",
  "level": 2,
  "level_text": "보통",
  "total_score": 34.8,
  "detail_title": "차선 수와 교차로 복잡도가 반영됨",
  "detail_description": "교차로 밀집도와 도로 규모가 난이도에 영향을 주는 구간입니다.",
  "score_breakdown": {
    "accident_rate_score": 30.0,
    "road_shape_score": 25.0,
    "road_scale_score": 50.0,
    "intersection_score": 40.0,
    "traffic_volume_score": 29.0
  },
  "evidence": [],
  "updated_at": "2026-03-21T22:00:00"
}
```

### 4.5 도로별 취약특성 상세 조회
- Method: `GET`
- Path: `/map/segments/{segment_id}/vulnerabilities`
- Auth: 없음

Response `200 OK`

```json
{
  "segment_id": "SEG_000001",
  "count": 1,
  "items": [
    {
      "vulnerability_type_id": 1,
      "code": "AVOID_HIGHWAY",
      "name": "고속도로 회피",
      "description": "고속/전용도로 주행 부담",
      "icon_key": "highway",
      "severity": 0.8,
      "note": "고속 주행 부담 가능",
      "source": "rule-based"
    }
  ]
}
```

## 5. 도로 세그먼트 기본 API

### 5.1 도로 세그먼트 조회
- Method: `GET`
- Path: `/api/segments`
- Auth: 없음

Query Params
- `minLat`, `maxLat`, `minLon`, `maxLon` 필수

Response `200 OK`

```json
[
  {
    "segmentId": "SEG_000001",
    "name": "테헤란로",
    "centerLat": 37.501,
    "centerLon": 127.039,
    "level": 2,
    "levelText": "보통",
    "totalScore": 34.8
  }
]
```

### 5.2 도로 상세 조회
- Method: `GET`
- Path: `/api/segments/{id}`
- Auth: 없음

Response `200 OK`

```json
{
  "segmentId": "SEG_000001",
  "name": "테헤란로",
  "highway": "primary",
  "startLat": 37.5009,
  "startLon": 127.0388,
  "endLat": 37.5011,
  "endLon": 127.0392,
  "centerLat": 37.501,
  "centerLon": 127.039,
  "coordinatesJson": "[[127.0388,37.5009],[127.0392,37.5011]]",
  "level": 2,
  "levelText": "보통",
  "totalScore": 34.8,
  "explanation": "차선 수와 교차로 복잡도가 반영됨",
  "detailDescription": "교차로 밀집도와 도로 규모가 난이도에 영향을 주는 구간입니다.",
  "computedAt": "2026-03-21T22:00:00"
}
```

### 5.3 도로 툴팁 요약
- Method: `GET`
- Path: `/api/segments/{id}/tooltip`
- Auth: 없음

Response `200 OK`

```json
{
  "segmentId": "SEG_000001",
  "name": "테헤란로",
  "level": 2,
  "levelText": "보통",
  "totalScore": 34.8,
  "explanation": "차선 수와 교차로 복잡도가 반영됨"
}
```

### 5.4 도로 난이도 조회
- Method: `GET`
- Path: `/api/segments/{id}/difficulty`
- Auth: 없음

Response `200 OK`

```json
{
  "segmentId": "SEG_000001",
  "level": 2,
  "levelText": "보통",
  "totalScore": 34.8
}
```

### 5.5 도로 난이도 구성요소 조회
- Method: `GET`
- Path: `/api/segments/{id}/score-detail`
- Auth: 없음

Response `200 OK`

```json
{
  "segmentId": "SEG_000001",
  "totalScore": 34.8,
  "accidentRateScore": 30.0,
  "roadShapeScore": 25.0,
  "roadScaleScore": 50.0,
  "intersectionScore": 40.0,
  "trafficVolumeScore": 29.0
}
```

## 6. OSM 경로 탐색

### 6.1 경로 탐색
- Method: `POST`
- Path: `/api/routes/search`
- Auth: 선택

설명:
- `vulnerabilities`를 직접 넘기면 그 값 사용
- `vulnerabilities`를 생략하고 인증 헤더를 보내면 사용자 설문 결과를 자동 반영

Request

```json
{
  "startLat": 37.4979,
  "startLon": 127.0276,
  "endLat": 37.5172,
  "endLon": 127.0473,
  "vulnerabilities": ["AVOID_HIGHWAY", "AVOID_COMPLEX_INTERSECTION"]
}
```

Response `200 OK`

```json
{
  "totalDistanceM": 3150,
  "estimatedMinutes": 9,
  "avgDifficulty": 31.4,
  "segments": [
    {
      "edgeId": "E_1024",
      "name": "테헤란로",
      "highway": "primary",
      "lengthM": 220.5,
      "difficulty": 34.8,
      "coordinatesJson": "[[127.0388,37.5009],[127.0392,37.5011]]"
    }
  ]
}
```

### 6.2 그래프 상태 확인
- Method: `GET`
- Path: `/api/routes/status`
- Auth: 없음

Response `200 OK`

```json
{
  "graphReady": true
}
```

## 7. 테스트용 계정

```text
email: test@test.com
password: 1234
```
