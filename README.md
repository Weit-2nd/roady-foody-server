# Roady Foody Server

길거리 음식점 정보를 사용자들이 함께 만들어가는 크라우드소싱 기반 플랫폼 서버

## 프로젝트 소개

**Roady Foody(로디푸디)** 는 붕어빵, 타코야키, 호떡 등 길거리 음식 판매 장소를 사용자들이 직접 리포트하고 공유하는 서비스입니다. 유동적인 길거리 음식점의 특성을 반영하여 실시간으로 영업 상태를 파악하고, 신뢰할 수 있는 리뷰 시스템을 제공합니다.

### 핵심 기능

#### 음식점 리포트 시스템
- 사용자가 직접 길거리 음식점 위치와 정보를 등록
- 영업 시간, 카테고리, 사진 등 상세 정보 제공
- 위치 기반으로 주변 음식점 검색 (기본 250m 반경)

#### 실시간 영업 상태 확인
- "열었어요" / "닫았어요" 리포트를 통해 실시간 영업 상태 파악
- 누적된 데이터를 통해 예상 영업 시간 및 휴무일 패턴 분석
- 주변 사용자에게 영업 확인 요청 알림 발송

#### 리워드 시스템
- 리포트 및 영업 확인 기여 시 코인(토큰) 지급
- 코인 사용 시 검색 반경 확대 (2배씩 증가)
- 기여도에 따른 배지 시스템 (초심자 → 중수 → 고수 → 초고수)

#### 신뢰성 있는 리뷰 시스템
- 자기 홍보 방지: 4점 이상 리뷰가 5개 이상일 때만 리뷰 노출
- 지역 변경 후: 4점 이상 리뷰 3건 이상 작성 시 리뷰 노출
- 리뷰 좋아요 및 신고 기능

#### 로컬 맛집 컨셉
- 지역 설정 후 6개월간 변경 불가
- 해당 지역의 진정한 로컬 정보 제공

## 기술 스택

### Backend
- **Language:** Kotlin 2.0.0
- **Framework:** Spring Boot 3.3.0
- **JDK:** Java 21

### Database & Storage
- **Main DB:** Oracle Database
- **Cache:** Redis (Redisson)
- **Search Engine:** OpenSearch
- **File Storage:** AWS S3
- **Migration:** Flyway

### Infrastructure
- **Cloud:** AWS (S3, Parameter Store)
- **Container:** Docker
- **CI/CD:** GitHub Actions
- **Monitoring:** Prometheus, Sentry

### Core Libraries
- **ORM:** Spring Data JPA, Hibernate Spatial
- **Query DSL:** Kotlin-JDSL
- **Auth:** Spring Security, JWT
- **API Docs:** SpringDoc OpenAPI (Swagger)
- **Resilience:** Resilience4J (Circuit Breaker)

## 프로젝트 구조

```
src/main/kotlin/kr/weit/roadyfoody/
├── admin/                 # 관리자 기능
├── auth/                  # 인증/인가 (JWT, 소셜 로그인)
├── badge/                 # 배지 시스템
├── common/                # 공통 코드
├── foodSpots/             # 음식점 정보 관리
├── global/                # 글로벌 설정 및 유틸
├── ranking/               # 랭킹 시스템
├── review/                # 리뷰 시스템
├── rewards/               # 리워드/코인 시스템
├── search/                # 검색 기능
│   ├── address/           # 주소 검색
│   ├── foodSpots/         # 음식점 검색
│   └── tourism/           # 관광지 검색
├── term/                  # 약관 관리
├── user/                  # 사용자 관리
└── useragreedterm/        # 사용자 약관 동의
```

## API 엔드포인트

### 인증 (`/api/v1/auth`)
| Method | Endpoint                      | Description |
|--------|-------------------------------|-------------|
| POST   | `/api/v1/auth`                | 회원가입        |
| GET    | `/api/v1/auth`                | 소셜 로그인      |
| GET    | `/api/v1/auth/check-nickname` | 닉네임 중복 확인   |
| GET    | `/api/v1/auth/refresh`        | 토큰 갱신       |
| POST   | `/api/v1/auth/sign-out`       | 로그아웃        |
| POST   | `/api/v1/auth/withdraw`       | 회원탈퇴        |

### 사용자 (`/api/v1/users`)
| Method | Endpoint                                      | Description |
|--------|-----------------------------------------------|-------------|
| GET    | `/api/v1/users/me`                            | 내 정보 조회     |
| GET    | `/api/v1/users/{userId}/food-spots/histories` | 사용자 리포트 이력  |
| GET    | `/api/v1/users/{userId}/reviews`              | 사용자 리뷰      |
| GET    | `/api/v1/users/{userId}/likes/reviews`        | 좋아요한 리뷰     |
| GET    | `/api/v1/users/{userId}/statistics`           | 사용자 통계      |
| PATCH  | `/api/v1/users/nickname`                      | 닉네임 수정      |
| PATCH  | `/api/v1/users/profile`                       | 프로필 이미지 수정  |

### 음식점 (`/api/v1/food-spots`)
| Method | Endpoint                                   | Description |
|--------|--------------------------------------------|-------------|
| POST   | `/api/v1/food-spots`                       | 음식점 리포트 생성  |
| GET    | `/api/v1/food-spots/{foodSpotsId}`         | 음식점 상세 정보   |
| PATCH  | `/api/v1/food-spots/{foodSpotsId}`         | 음식점 정보 수정   |
| GET    | `/api/v1/food-spots/{foodSpotsId}/reviews` | 음식점 리뷰 조회   |
| GET    | `/api/v1/food-spots/search`                | 음식점 검색      |
| GET    | `/api/v1/food-spots/popular-searches`      | 인기 검색어      |

### 리뷰 (`/api/v1/review`)
| Method | Endpoint                           | Description |
|--------|------------------------------------|-------------|
| POST   | `/api/v1/review`                   | 리뷰 작성       |
| PATCH  | `/api/v1/review/{reviewId}`        | 리뷰 수정       |
| DELETE | `/api/v1/review/{reviewId}`        | 리뷰 삭제       |
| POST   | `/api/v1/reviews/{reviewId}/likes` | 좋아요 토글      |

### 랭킹 (`/api/v1/ranking`)
| Method | Endpoint                 | Description |
|--------|--------------------------|-------------|
| GET    | `/api/v1/ranking/report` | 리포트 랭킹      |
| GET    | `/api/v1/ranking/review` | 리뷰 랭킹       |
| GET    | `/api/v1/ranking/like`   | 좋아요 랭킹      |
| GET    | `/api/v1/ranking/total`  | 통합 랭킹       |

### 리워드 (`/api/v1/rewards`)
| Method | Endpoint             | Description |
|--------|----------------------|-------------|
| GET    | `/api/v1/rewards/me` | 내 리워드 조회    |

## 시작하기

### 사전 요구사항
- JDK 21
- Gradle 8.x
- Docker (테스트용)
- Oracle Database
- Redis
- OpenSearch

### 빌드
```bash
./gradlew build
```

### 테스트
```bash
./gradlew test
```

### 로컬 실행
```bash
./gradlew bootRun
```

### Docker 빌드 및 실행
```bash
docker build -t roady-foody .
docker run -e APP_PHASE=sandbox -p 8080:8080 roady-foody
```

## 환경별 설정

| 프로필       | 용도         |
|-----------|------------|
| `test`    | 테스트 환경     |
| `sandbox` | 샌드박스/개발 환경 |
| `stable`  | 프로덕션 환경    |

## 배지 시스템

| 배지             | 조건                       |
|----------------|--------------------------|
| 초심자 (BEGINNER) | 시작 배지                    |
| 중수 (PRO)       | 리뷰 5개 이상, 고등급 리뷰 3개 이상   |
| 고수 (SUPER)     | 리뷰 10개 이상, 고등급 리뷰 5개 이상  |
| 초고수 (EXPERT)   | 리뷰 20개 이상, 고등급 리뷰 10개 이상 |

*고등급 리뷰: 3점 이상의 리뷰*

## API 문서

Swagger UI: `http://localhost:8080/swagger-ui.html`
