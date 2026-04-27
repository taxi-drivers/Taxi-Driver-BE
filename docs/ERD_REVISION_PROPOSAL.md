# ERD Revision Proposal

## Current Alignment

The current DBML is a solid base for these domains:

- user profile
- vulnerability type lookup
- user-to-vulnerability mapping
- level rule management
- road segment rendering, scoring, and vulnerability overlay

These tables are already enough to support most map APIs:

- `users`
- `user_vulnerability_map`
- `vulnerability_type`
- `level_rule`
- `road_segments`
- `segment_vulnerability_map`

## Gaps Against API Scope

The API scope in `docs/API_SPEC.csv` is wider than the current DBML. The following areas still need schema support.

### 1. Survey Question Management

Current DBML can store only the final survey result on `users` and `user_vulnerability_map`.
It cannot store survey questions, option metadata, or answer history.

Recommended tables:

```dbml
Table survey_question {
  survey_question_id bigint [pk, increment]
  code varchar(50) [not null, unique]
  question_text varchar(255) [not null]
  question_type varchar(30) [not null, note: "single_choice, multi_choice, scale"]
  display_order int [not null]
  is_active boolean [not null, default: true]
  created_at datetime [not null]
}

Table survey_option {
  survey_option_id bigint [pk, increment]
  survey_question_id bigint [not null]
  code varchar(50) [not null]
  option_text varchar(255) [not null]
  score_value int
  vulnerability_type_id int
  display_order int [not null]
}

Table survey_submission {
  survey_submission_id bigint [pk, increment]
  user_id bigint [not null]
  skill_level int [not null]
  primary_vulnerability_type_id int
  client_version varchar(50)
  submitted_at datetime [not null]
}

Table survey_answer {
  survey_submission_id bigint [not null]
  survey_question_id bigint [not null]
  survey_option_id bigint [not null]

  Indexes {
    (survey_submission_id, survey_question_id, survey_option_id) [pk]
  }
}
```

### 2. Authentication

Current DBML has `email`, but it does not define how login identity is stored.
The current backend code still assumes password-based local login, while the project plan says Google OAuth2 later.

Recommended direction:

- keep `users` as the profile table
- move auth identity into a separate table

```dbml
Table user_auth_account {
  user_auth_account_id bigint [pk, increment]
  user_id bigint [not null]
  provider varchar(30) [not null, note: "LOCAL, GOOGLE"]
  provider_user_key varchar(255) [note: "OAuth provider subject/id"]
  password_hash varchar(255) [note: "LOCAL login only"]
  is_active boolean [not null, default: true]
  created_at datetime [not null]
  updated_at datetime

  Indexes {
    (provider, provider_user_key) [unique]
  }
}
```

If the project will skip local login entirely, `password_hash` can be removed and only OAuth columns kept.

### 3. Route Search and Route Persistence

The API spec includes route search and route detail, but the current DBML has no route storage.

Recommended tables:

```dbml
Table routes {
  route_id bigint [pk, increment]
  user_id bigint [not null]
  origin_name varchar(255)
  destination_name varchar(255)
  origin_lat double [not null]
  origin_lon double [not null]
  destination_lat double [not null]
  destination_lon double [not null]
  total_distance_m int
  estimated_time_sec int
  average_difficulty double
  route_polyline_json json [not null]
  created_at datetime [not null]
}

Table route_segment_map {
  route_id bigint [not null]
  segment_id varchar(50) [not null]
  sequence_no int [not null]

  Indexes {
    (route_id, sequence_no) [pk]
    (route_id, segment_id)
  }
}
```

## Recommended Final Shape

### Keep as core tables

- `users`
- `user_vulnerability_map`
- `vulnerability_type`
- `level_rule`
- `road_segments`
- `segment_vulnerability_map`

### Add for full API coverage

- `survey_question`
- `survey_option`
- `survey_submission`
- `survey_answer`
- `user_auth_account`
- `routes`
- `route_segment_map`

## Backend Refactor Notes

The backend should treat `road_segments` as the single source of truth for:

- map rendering coordinates
- difficulty score
- level
- score breakdown
- explanation
- evidence

That means legacy split-table models such as `segment_score` and `segment_level` should remain removed.
