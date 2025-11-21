# 🤖 CodeReview AI Assistant

> AI 기반 실시간 코드 리뷰 자동화 시스템

개인 개발자와 소규모 팀을 위한 AI 기반 자동 코드 리뷰 시스템입니다. GitHub, GitLab, Bitbucket의 Pull Request/Merge Request 생성 시 자동으로 코드를 분석하고, 버그, 성능 이슈, 보안 취약점, 베스트 프랙티스 위반 등을 감지하여 즉각적인 피드백을 제공합니다.

---

## 📋 목차

- [프로젝트 개요](#-프로젝트-개요)
- [프로젝트 목표](#-프로젝트-목표)
- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [시작하기](#-시작하기)
- [API 문서](#-api-문서)
- [프로젝트 구조](#-프로젝트-구조)
- [개발 로드맵](#-개발-로드맵)
- [기여하기](#-기여하기)

---

## 🎯 프로젝트 개요

### 배경

개인 프로젝트나 소규모 팀에서 체계적인 코드 리뷰가 부재한 상황에서, LLM 기술의 발전으로 맥락을 이해하는 고품질 코드 분석이 가능해졌습니다. 이를 활용하여 개발자의 코드 품질 향상과 학습을 돕는 자동화 시스템을 구축했습니다.

### 핵심 가치 제안

1. **즉각적 피드백**: PR 생성 후 1분 이내 자동 리뷰
2. **학습 중심 리뷰**: 단순 지적이 아닌 '왜'와 '어떻게'를 설명하는 교육적 리뷰
3. **맞춤형 분석**: 프로젝트 컨텍스트를 이해한 맥락 기반 리뷰
4. **비용 효율성**: gpt-4o-mini 모델 사용으로 리뷰당 약 $0.01 비용

### 타겟 사용자

- 개인 개발자
- 주니어 개발자
- 소규모 개발팀

---

## 🎯 프로젝트 목표

### 단기 목표 (완료 ✅)

- [x] GitHub, GitLab, Bitbucket 멀티 플랫폼 지원
- [x] AI 기반 자동 코드 리뷰 엔진 구축
- [x] 실시간 웹훅 기반 PR/MR 감지 및 처리
- [x] 대시보드를 통한 리뷰 통계 및 인사이트 제공
- [x] 커스텀 리뷰 규칙 엔진 구현

---

## ✨ 주요 기능

### 1. 멀티 플랫폼 지원

- **GitHub**: GitHub App 및 Personal Access Token 지원
- **GitLab**: Personal Access Token 지원
- **Bitbucket**: App Password 지원

각 플랫폼의 웹훅을 통한 실시간 PR/MR 이벤트 감지 및 자동 처리

### 2. AI 기반 코드 분석

다음 영역을 자동으로 분석합니다:

- **코드 품질**: 가독성, 네이밍, 구조
- **잠재 버그**: NPE, 리소스 누수, 경쟁 조건
- **성능 이슈**: 비효율적 로직, N+1 문제
- **보안 취약점**: SQL Injection, XSS, 하드코딩된 자격증명
- **베스트 프랙티스**: 디자인 패턴, SOLID 원칙

### 3. 언어별 특화 리뷰

다음 프로그래밍 언어에 대한 특화된 리뷰를 제공합니다:

- Java (Spring Boot, JPA/Hibernate)
- Python (Django, Flask, FastAPI)
- JavaScript/TypeScript (Node.js, React, Vue)
- Go
- Rust
- C++

각 언어별로 최적화된 프롬프트를 사용하여 더 정확한 리뷰를 제공합니다.

### 4. 자동 코멘트 생성

- **라인별 코멘트**: 정확한 위치에 피드백 제공
- **PR 요약**: 전체 리뷰 종합 리포트
- **개선 제안**: 구체적인 코드 예시 포함

### 5. 커스텀 리뷰 규칙

팀별 코딩 컨벤션과 규칙을 설정하여 맞춤형 리뷰를 받을 수 있습니다.

### 6. 대시보드 & 통계

- **리뷰 히스토리**: 과거 리뷰 조회 및 검색
- **통계 대시보드**: Repository별, 기간별 통계
- **트렌드 분석**: 코드 품질 트렌드 시각화
- **심각도별 분포**: Critical, High, Medium, Low 이슈 분포

### 7. 성능 최적화

- **토큰 최적화**: 4K diff 제한, 1.5K 응답 제한
- **스마트 필터링**: lock 파일, 바이너리, 생성 코드 자동 제외
- **비동기 처리**: RabbitMQ를 통한 큐 기반 처리
- **캐싱**: Redis를 통한 반복 분석 방지

---

## 🛠 기술 스택

### Backend

- **Framework**: Spring Boot 3.2
- **Language**: Java 17
- **Build Tool**: Gradle
- **API Documentation**: SpringDoc OpenAPI (Swagger)

### AI Integration

- **Spring AI**: LLM 통합 프레임워크
- **Provider**: OpenAI API (gpt-4o-mini)
- **Token Optimization**: 공격적인 토큰 최적화로 비용 절감

### Infrastructure

- **Database**: PostgreSQL 15 (프로덕션), H2 (로컬 개발)
- **Cache**: Redis 7
- **Queue**: RabbitMQ 3.12
- **Migration**: Flyway
- **Container**: Docker & Docker Compose

### Frontend

- **Template Engine**: Thymeleaf
- **Styling**: Custom CSS
- **Charts**: Chart.js

### Monitoring & Health

- **Spring Actuator**: Health Check, Metrics
- **Prometheus**: 메트릭 수집

---

## 🚀 시작하기

### 사전 요구사항

- Java 17 이상
- Docker & Docker Compose (프로덕션 환경)
- GitHub App 또는 Personal Access Token
- OpenAI API Key

### 환경 설정

#### 1. 환경 변수 파일 생성

```bash
# .env 파일 생성 (프로젝트 루트)
cat > .env << EOF
# 데이터베이스 설정
DB_USERNAME=postgres
DB_PASSWORD=your_strong_password

# Redis 설정
REDIS_PASSWORD=your_redis_password

# RabbitMQ 설정
RABBITMQ_USERNAME=your_rabbitmq_user
RABBITMQ_PASSWORD=your_rabbitmq_password

# OpenAI API 키 설정
OPENAI_API_KEY=your_openai_api_key

# GitHub App 설정
GITHUB_APP_ID=your_app_id
GITHUB_PRIVATE_KEY=your_private_key
GITHUB_WEBHOOK_SECRET=your_webhook_secret

# GitLab 설정 (선택)
GITLAB_API_URL=https://gitlab.com/api/v4
GITLAB_TOKEN=your_gitlab_token

# Bitbucket 설정 (선택)
BITBUCKET_API_URL=https://api.bitbucket.org/2.0
BITBUCKET_USERNAME=your_bitbucket_username
BITBUCKET_APP_PASSWORD=your_bitbucket_app_password
EOF
```

**⚠️ 보안 주의사항:**

- 모든 환경 변수는 **필수**입니다. 설정하지 않으면 애플리케이션이 시작되지 않습니다.
- 프로덕션 환경에서는 반드시 강력한 비밀번호를 사용하세요.
- `.env` 파일은 절대 Git에 커밋하지 마세요 (이미 `.gitignore`에 포함됨).

### 실행 방법

#### 방법 1: Docker Compose로 실행 (권장 - 프로덕션)

```bash
# 모든 서비스 시작 (PostgreSQL, Redis, RabbitMQ, App)
docker-compose up -d

# 로그 확인
docker-compose logs -f app

# 서비스 중지
docker-compose down

# 볼륨까지 삭제
docker-compose down -v
```

#### 방법 2: 로컬 개발 환경 (Docker 없이)

```bash
# PostgreSQL, Redis, RabbitMQ만 Docker로 실행
docker-compose up -d postgres redis rabbitmq

# 애플리케이션 실행
./gradlew bootRun --args='--spring.profiles.active=local'
```

로컬 개발 환경에서는 H2 인메모리 데이터베이스를 사용할 수 있습니다. 자세한 내용은 [로컬 실행 가이드](LOCAL_RUN_GUIDE.md)를 참조하세요.

#### 방법 3: 스크립트 실행

```bash
# 실행 권한 부여 (최초 1회)
chmod +x run-local.sh

# 애플리케이션 실행
./run-local.sh
```

### 접속 정보

애플리케이션이 시작되면 다음 주소로 접속 가능합니다:

| 서비스            | URL                                         | 설명                   |
| ----------------- | ------------------------------------------- | ---------------------- |
| **메인 대시보드** | http://localhost:8080                       | 프론트엔드 UI          |
| **API 문서**      | http://localhost:8080/swagger-ui/index.html | Swagger UI             |
| **Health Check**  | http://localhost:8080/actuator/health       | 상태 확인              |
| **Metrics**       | http://localhost:8080/actuator/metrics      | 애플리케이션 메트릭    |
| **RabbitMQ 관리** | http://localhost:15672                      | RabbitMQ Management UI |

---

## 📚 API 문서

### Webhook 엔드포인트

#### GitHub Webhook

- `POST /api/webhook/github` - GitHub Webhook 수신
- `GET /api/webhook/health` - GitHub Webhook Health Check

#### GitLab Webhook

- `POST /api/webhook/gitlab` - GitLab Webhook 수신
- `GET /api/webhook/gitlab/health` - GitLab Webhook Health Check

#### Bitbucket Webhook

- `POST /api/webhook/bitbucket` - Bitbucket Webhook 수신
- `GET /api/webhook/bitbucket/health` - Bitbucket Webhook Health Check

### Dashboard & Analytics

- `GET /api/dashboard/statistics` - 대시보드 전체 통계
- `GET /api/dashboard/reviews/recent?limit=10` - 최근 리뷰 목록
- `GET /api/dashboard/trends?days=30` - 트렌드 데이터 (일별 리뷰/코멘트/이슈)
- `GET /api/dashboard/repositories/statistics` - Repository별 통계
- `GET /api/dashboard/reviews/{reviewId}` - 리뷰 상세 정보 조회

### Custom Review Rules

- `GET /api/rules` - 리뷰 규칙 목록 조회
- `POST /api/rules` - 새 규칙 생성
- `PUT /api/rules/{id}` - 규칙 수정
- `DELETE /api/rules/{id}` - 규칙 삭제

### Monitoring

- `GET /actuator/health` - Actuator Health Check
- `GET /actuator/metrics` - Application Metrics
- `GET /actuator/info` - Application Info
- `GET /actuator/prometheus` - Prometheus Metrics

자세한 API 문서는 Swagger UI (http://localhost:8080/swagger-ui/index.html)에서 확인할 수 있습니다.

---

## 🔧 플랫폼별 설정 가이드

### GitHub App 설정

1. GitHub에서 새 GitHub App 생성
   - Settings → Developer settings → GitHub Apps → New GitHub App
2. Webhook URL 설정: `https://your-domain.com/api/webhook/github`
3. 필요한 권한 설정:
   - Repository permissions:
     - Contents: Read
     - Pull requests: Read & Write
     - Metadata: Read (기본)
4. Webhook 이벤트 선택:
   - Pull request
   - Pull request review
5. GitHub App 설치 및 권한 부여

### GitLab 설정

1. GitLab에서 Personal Access Token 생성
   - Settings → Access Tokens
   - Scopes: `api`, `read_api`
2. 환경 변수 설정:
   ```bash
   GITLAB_API_URL=https://gitlab.com/api/v4
   GITLAB_TOKEN=your_gitlab_token
   ```
3. Webhook URL 설정: `https://your-domain.com/api/webhook/gitlab`
4. Webhook 이벤트 선택: Merge Request events

### Bitbucket 설정

1. Bitbucket에서 App Password 생성
   - Personal settings → App passwords
2. 환경 변수 설정:
   ```bash
   BITBUCKET_API_URL=https://api.bitbucket.org/2.0
   BITBUCKET_USERNAME=your_bitbucket_username
   BITBUCKET_APP_PASSWORD=your_bitbucket_app_password
   ```
3. Webhook URL 설정: `https://your-domain.com/api/webhook/bitbucket`
4. Webhook 이벤트 선택: Pull request events

자세한 설정 방법은 [Phase 3 가이드](docs/PHASE3_GUIDE.md)를 참조하세요.

---

## 📁 프로젝트 구조

```
src/
├── main/
│   ├── java/com/codereview/assistant/
│   │   ├── config/          # 설정 클래스
│   │   │   ├── OpenAiConfig.java
│   │   │   ├── RabbitMQConfig.java
│   │   │   ├── SecurityConfig.java
│   │   │   └── ...
│   │   ├── controller/      # REST 컨트롤러
│   │   │   ├── WebhookController.java
│   │   │   ├── DashboardController.java
│   │   │   ├── ReviewRuleController.java
│   │   │   └── ...
│   │   ├── domain/          # JPA 엔티티
│   │   │   ├── Repository.java
│   │   │   ├── PullRequest.java
│   │   │   ├── Review.java
│   │   │   ├── Comment.java
│   │   │   └── ReviewRule.java
│   │   ├── repository/      # JPA 레포지토리
│   │   │   ├── RepositoryRepository.java
│   │   │   ├── PullRequestRepository.java
│   │   │   ├── ReviewRepository.java
│   │   │   └── ...
│   │   ├── service/         # 비즈니스 로직
│   │   │   ├── ReviewService.java
│   │   │   ├── CodeReviewService.java
│   │   │   ├── GitHubWebhookService.java
│   │   │   ├── StatisticsService.java
│   │   │   └── ...
│   │   ├── dto/             # DTO 클래스
│   │   │   ├── CodeReviewResult.java
│   │   │   ├── DashboardStatistics.java
│   │   │   └── ...
│   │   └── exception/       # 예외 처리
│   │       ├── ResourceNotFoundException.java
│   │       └── ...
│   └── resources/
│       ├── application.yml  # 애플리케이션 설정
│       ├── application-local.yml  # 로컬 개발 설정
│       ├── application-docker.yml # Docker 설정
│       ├── db/migration/    # 데이터베이스 마이그레이션
│       │   ├── V1__initial_schema.sql
│       │   └── V2__add_review_rules.sql
│       ├── templates/       # Thymeleaf 템플릿
│       │   └── dashboard.html
│       └── static/          # 정적 리소스
│           ├── css/
│           └── js/
└── test/                    # 테스트 코드
    └── java/com/codereview/assistant/
        ├── controller/
        └── service/
```

---

## 🗺 개발 로드맵

### Phase 1 ✅ (완료)

- [x] 프로젝트 기본 구조 설정
- [x] GitHub Webhook 통합
- [x] AI 코드 분석 엔진
- [x] 자동 코멘트 생성
- [x] 기본 대시보드 UI

### Phase 2 ✅ (완료)

- [x] 대시보드 백엔드 API
- [x] 리뷰 통계 및 인사이트
- [x] 커스텀 리뷰 규칙 엔진
- [x] 단위 및 통합 테스트
- [x] 테스트 및 API 문서

### Phase 3 ✅ (완료)

- [x] GitLab/Bitbucket 지원
- [x] 멀티 언어 지원 확대 (Java, Python, JavaScript, TypeScript, Go, Rust, C++)
- [x] 언어별 특화 프롬프트
- [x] 성능 최적화 (토큰 비용 절감)

### Phase 4 (진행 중)

- [x] 대시보드 UI 개선 (반응형 디자인)
- [x] OpenAI API 상태 확인 엔드포인트
- [x] 프롬프트 구조화 및 품질 향상
- [ ] 프론트엔드 대시보드 UI (React)

### Phase 5 (계획)

- [ ] 코드 자동 수정 제안
- [ ] 학습 기반 개인화 리뷰
- [ ] 팀 협업 기능 강화
- [ ] 보안 취약점 심화 분석

---

## 🧪 테스트

### 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests StatisticsServiceTest

# 테스트 커버리지 리포트 생성
./gradlew test jacocoTestReport

# 테스트 리포트 확인
open build/reports/tests/test/index.html
```

### 테스트 문서

- **[테스트 가이드](docs/TESTING.md)** - 단위/통합 테스트 작성 및 실행 방법
- **[API 테스트 가이드](docs/API_TESTING.md)** - REST API 테스트 방법 및 예제
- **[Docker 테스트 가이드](docs/DOCKER_TESTING.md)** - Docker 환경 테스트 방법

---

## 📊 데이터베이스 마이그레이션

Flyway를 사용하여 자동으로 데이터베이스 스키마가 생성됩니다.

마이그레이션 파일 위치: `src/main/resources/db/migration/`

- `V1__initial_schema.sql` - 초기 스키마 (Repository, PullRequest, Review, Comment)
- `V2__add_review_rules.sql` - 리뷰 규칙 테이블 추가

---

## 🤝 기여하기

PR과 이슈는 언제나 환영합니다!

### 기여 방법

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

자세한 내용은 [기여 가이드](docs/CONTRIBUTING.md)를 참조하세요.

---

## 📖 추가 문서

- [로컬 실행 가이드](LOCAL_RUN_GUIDE.md) - Docker 없이 로컬에서 실행하는 방법
- [배포 가이드](docs/DEPLOYMENT.md) - 프로덕션 배포 방법
- [통합 가이드](INTEGRATION_GUIDE.md) - 플랫폼별 통합 방법
- [Phase 3 가이드](docs/PHASE3_GUIDE.md) - GitLab/Bitbucket 설정 가이드

---

## 📝 라이선스

MIT License

---

## 📧 문의

이슈 탭에서 질문이나 버그를 제보해주세요.

---

**즐거운 코딩하세요! 🎉**
