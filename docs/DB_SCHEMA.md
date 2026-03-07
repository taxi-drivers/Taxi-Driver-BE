// dbdiagram.io (DBML)



// -----------------------------
// 사용자 (기본 정보 + 프로필 통합) ✅
// -----------------------------
Table users {
  user_id bigint [pk, note: "사용자 고유 ID"]
  email varchar(255) [not null, unique, note: "로그인/식별 이메일"]
  nickname varchar(50) [note: "표시용 닉네임"]

  skill_level int [not null, note: "운전 숙련도 점수(설문 산출값, 예: 0~100)"]
  vulnerability_type_id int [note: "사용자가 선택한 취약 특성 ID(단일 선택 가정)"]

  created_at datetime [not null, note: "가입 시각"]
  updated_at datetime [note: "정보 수정 시각"]
}

// -----------------------------
// 사용자-취약특성 매핑
// -----------------------------
Table user_vulnerability_map {
  user_id bigint [not null]
  vulnerability_type_id int [not null]


  created_at datetime [not null]

  Indexes {
    (user_id, vulnerability_type_id) [pk]
  }
}



// -----------------------------
// 취약 특성 정의
// -----------------------------
Table vulnerability_type {
  vulnerability_type_id int [pk, increment, note: "취약 특성 타입 ID"]
  code varchar(50) [not null, unique, note: "예: AVOID_HIGHWAY, PREFER_WIDE_ROAD, AVOID_COMPLEX_INTERSECTION"]
  name varchar(100) [not null, note: "표시 이름(한글)"]
  description text [note: "클릭 상세 설명"]
  icon_key varchar(50) [note: "프론트 아이콘 키(선택)"]
}


// -----------------------------
// Level(1~3) 계산식 정의 (버전 관리)
// -----------------------------
Table level_rule {
  level_rule_id bigint [pk, increment, note: "레벨 산출 규칙 ID"]
  version varchar(30) [not null, unique, note: "규칙 버전 (예: v1, 2026-02-11)"]
  name varchar(80) [not null, note: "규칙 이름 (예: MVP-가중합-임계값)"]

  w_accident_rate double [not null, note: "사고율 점수에 적용되는 가중치"]
  w_road_shape double [not null, note: "도로형태 점수에 적용되는 가중치"]
  w_road_scale double [not null, note: "도로규모 점수에 적용되는 가중치"]
  w_intersection double [not null, note: "교차로 점수에 적용되는 가중치"]
  w_traffic_volume double [not null, note: "교통량 점수에 적용되는 가중치"]

  level1_max double [not null, note: "Level 1(쉬움)의 최대 점수 상한값"]
  level2_max double [not null, note: "Level 2(보통)의 최대 점수 상한값 (이 초과 시 Level 3)"]

  is_active boolean [not null, default: true, note: "현재 운영에 적용 중인 규칙 여부"]
  created_at datetime [not null, note: "규칙 생성 시각"]
}



// -----------------------------
// 도로 세그먼트 (지도 렌더링 + 점수 + 근거 통합) 
// -----------------------------
Table road_segments {
  segment_id varchar(50) [pk, note: "세그먼트 ID (예: SEG_000001)"]

  // 지도/도로 기본 정보
  name varchar(120) [note: "도로명"]
  highway varchar(30) [note: "도로 유형/등급"]
  start_lat double [not null]
  start_lon double [not null]
  end_lat double [not null]
  end_lon double [not null]
  center_lat double
  center_lon double
  num_points int [note: "polyline 좌표 개수"]
  coordinates_json json [not null, note: "polyline 좌표 배열(지도 선 렌더링용)"]

  // (기존 segment_score 통합) 난이도 점수
  total_score double [not null, note: "최종 난이도 점수(0~100)"]
  level_text varchar(20) [note: "쉬움/보통/어려움(선택)"]

  accident_rate_score double [not null]
  road_shape_score double [not null]
  road_scale_score double [not null]
  intersection_score double [not null]
  traffic_volume_score double [not null]

  // Hover/Click 설명(요약/서술)
  explanation varchar(300) [note: "Hover용 요약(짧게)"]
  detail_description text [note: "Click용 상세 설명(서술)"]

  // (기존 segment_evidence 통합) 원천 근거 묶음
  // - 클릭 시 근거 카드 리스트로 렌더링
  evidence_json json [note: "원천 근거 카드 배열(JSON). 예: 주정차단속/경사+결빙/사고다발 등"]

  // Level 결과 + 적용 규칙 (segment_level 제거)
  level_rule_id bigint [not null, note: "level_rule.level_rule_id (적용된 규칙)"]
  level int [not null, note: "레벨 1~3 (1:쉬움, 2:보통, 3:어려움)"]
  level_score double [note: "가중합 산출 점수(디버깅/설명용)"]

  // 적재/계산 시각
  computed_at datetime [not null, note: "점수/레벨 계산 시각"]
  created_at datetime [not null, note: "적재 시각"]
  updated_at datetime [note: "갱신 시각"]

  Indexes {
    (level)
    (level_rule_id)
    (highway)
  }
}


// -----------------------------
// 세그먼트-취약특성 매핑 (오버레이)
// -----------------------------
Table segment_vulnerability_map {
  segment_id varchar(50) [not null, note: "road_segments.segment_id"]
  vulnerability_type_id int [not null, note: "vulnerability_type.vulnerability_type_id"]

  severity double [default: 1.0]
  note varchar(200)
  source varchar(100)
  updated_at datetime

  Indexes {
    (segment_id, vulnerability_type_id) [pk]
    (vulnerability_type_id)
  }
}


// ============================================================
// Relationships
// ============================================================
Ref: users.vulnerability_type_id > vulnerability_type.vulnerability_type_id
Ref: user_vulnerability_map.user_id > users.user_id
Ref: user_vulnerability_map.vulnerability_type_id > vulnerability_type.vulnerability_type_id
Ref: road_segments.level_rule_id > level_rule.level_rule_id

Ref: segment_vulnerability_map.segment_id > road_segments.segment_id
Ref: segment_vulnerability_map.vulnerability_type_id > vulnerability_type.vulnerability_type_id
