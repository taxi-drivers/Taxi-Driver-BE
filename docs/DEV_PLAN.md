# 개발 계획 요약 (캡스톤디자인2)

> 상세 계획: generate_plan.py (Excel 생성 스크립트) 참조
> 최종 아키텍처: React + Spring Boot + MySQL (Supabase 사용 안 함)

## 팀원 역할

| 담당 | 역할 | 비고 |
|------|------|------|
| **민규** | Frontend 100% | React + 카카오맵 |
| **준영** | DB(MySQL) + BE + Infra | Entity 설계, 데이터 임포트, Azure 배포 |
| **승종** | Backend 100% | 도로 데이터 API, 경로 API |
| **해석** | BE 핵심 (설문/알고리즘/인증) | A* 알고리즘, OAuth2(8주차~) |

## Phase별 일정

### Phase 1: 기반 구축 (1~2주차, 3/3 ~ 3/15)
- ERD 기반 Entity 클래스 설계 (JPA ddl-auto로 테이블 자동생성)
- MySQL에 erd_output 데이터 임포트
- Repository/Service 레이어 구현
- 설문/취약특성 API 착수 (mock 유저)

### Phase 2: 핵심 구현 (3~4주차, 3/16 ~ 3/29)
- 도로 세그먼트/난이도 조회 API 완성
- 좌표 기반 세그먼트 조회, bounds 쿼리
- 취약특성 상세 조회/수정 API
- 프론트-백엔드 최초 연결

### Phase 3: 프로토타입 (5주차, 3/30 ~ 4/5)
- 프론트-백엔드 전체 연결 (mock 유저)
- **[제출] 브리핑 영상 (4/4)**

### Phase 4: 경로 탐색 (6~8주차, 4/6 ~ 4/26)
- 그래프 자료구조 설계 (노드/엣지)
- A* 알고리즘 + 난이도 가중 cost 함수
- 경로 탐색 API, 옵션별 분기 (안전/빠른/최단)
- Azure VM 배포
- **8주차: OAuth2 + JWT 인증 구현**

### Phase 5: 고도화 (9~10주차, 4/27 ~ 5/10)
- 알고리즘 최적화
- API 문서화 (Swagger/OpenAPI)
- 통합 테스트 + 버그 수정

### Phase 6: 발표/마무리 (11~15주차, 5/11 ~ 6/14)
- 발표영상 + UCC + 판넬
- 최종발표 + 캡스톤 페스티벌
- 최종보고서

## 인증 전략

1~7주차는 **mock 유저** (하드코딩된 user_id=1)로 개발
8주차에 OAuth2 + JWT 인증 구현 후 실제 인증으로 전환

## 기술 스택

- **Backend**: Spring Boot 3.2.1, Java 17, Spring Data JPA, Lombok
- **Database**: MySQL 8.0 (localhost:3306/driving_db)
- **인증** (8주차~): Spring Security + OAuth2 + JWT
- **배포**: Azure VM
- **Frontend**: React 19 + TypeScript + Vite + 카카오맵 SDK
