# CodeReview AI Assistant

AI 기반 실시간 코드 리뷰 자동화 시스템

## 🧪 Test Mode
- TEST_MODE enabled for debugging
- Using gpt-4o-mini for 95% cost reduction
- Fixed test responses for validation

## 프로젝트 개요

개인 개발자와 소규모 팀을 위한 AI 기반 자동 코드 리뷰 시스템입니다. GitHub PR 생성 시 자동으로 코드를 분석하고, 품질 개선 사항을 제안합니다.

## 주요 기능

- **멀티 플랫폼 지원**: GitHub, GitLab, Bitbucket 웹훅 연동
- **AI 기반 코드 분석**: 버그, 성능, 보안, 베스트 프랙티스 체크
- **언어별 특화 리뷰**: Java, Python, JavaScript, TypeScript, Go, Rust, C++ 등
- **자동 코멘트 생성**: PR/MR에 자동으로 리뷰 코멘트 작성
- **커스텀 리뷰 규칙**: 팀별 코딩 규칙 적용
- **리뷰 히스토리 & 통계**: 대시보드를 통한 인사이트 제공

## 기술 스택

- **Backend**: Spring Boot 3.2, Java 17
- **Build**: Gradle
- **Database**: PostgreSQL 15
- **Cache**: Redis
- **Queue**: RabbitMQ
- **AI**: Spring AI + OpenAI API
- **Container**: Docker

## 시작하기

### 사전 요구사항

- Java 17 이상
- Docker & Docker Compose
- GitHub App 생성 및 설정
- OpenAI API Key

### 환경 설정

1. 환경 변수 파일 생성:
```bash
cp .env.example .env
```

2. `.env` 파일 수정:
```bash
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
```

**⚠️ 보안 주의사항:**
- 모든 환경 변수는 **필수**입니다. 설정하지 않으면 애플리케이션이 시작되지 않습니다.
- 프로덕션 환경에서는 반드시 강력한 비밀번호를 사용하세요.
- `.env` 파일은 절대 Git에 커밋하지 마세요 (이미 `.gitignore`에 포함됨).
- 기본 계정(postgres/guest)은 개발 환경에서만 사용하세요.

### Docker로 실행

```bash
# 모든 서비스 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f app

# 서비스 중지
docker-compose down
```

### 로컬 개발 환경

```bash
# PostgreSQL, Redis, RabbitMQ 시작
docker-compose up -d postgres redis rabbitmq

# 애플리케이션 실행
./gradlew bootRun
```

## 데이터베이스 마이그레이션

Flyway를 사용하여 자동으로 데이터베이스 스키마가 생성됩니다.

마이그레이션 파일 위치: `src/main/resources/db/migration/`

## 테스트

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

## API 엔드포인트

### GitHub Webhook
- `POST /api/webhook/github` - GitHub Webhook 수신
- `GET /api/webhook/health` - GitHub Webhook Health Check

### GitLab Webhook
- `POST /api/webhook/gitlab` - GitLab Webhook 수신
- `GET /api/webhook/gitlab/health` - GitLab Webhook Health Check

### Bitbucket Webhook
- `POST /api/webhook/bitbucket` - Bitbucket Webhook 수신
- `GET /api/webhook/bitbucket/health` - Bitbucket Webhook Health Check

### Dashboard & Analytics
- `GET /api/dashboard/statistics` - 대시보드 전체 통계
- `GET /api/dashboard/reviews/recent` - 최근 리뷰 목록
- `GET /api/dashboard/trends` - 트렌드 데이터 (일별 리뷰/코멘트/이슈)
- `GET /api/dashboard/repositories/statistics` - Repository별 통계

### Custom Review Rules
- `GET /api/rules` - 리뷰 규칙 목록 조회
- `POST /api/rules` - 새 규칙 생성
- `PUT /api/rules/{id}` - 규칙 수정
- `DELETE /api/rules/{id}` - 규칙 삭제

### Monitoring
- `GET /actuator/health` - Actuator Health Check
- `GET /actuator/metrics` - Application Metrics
- `GET /actuator/info` - Application Info

## 플랫폼별 설정 가이드

### GitHub App 설정

1. GitHub에서 새 GitHub App 생성
2. Webhook URL 설정: `https://your-domain.com/api/webhook/github`
3. 필요한 권한 설정:
   - Repository permissions:
     - Contents: Read
     - Pull requests: Read & Write
4. Webhook 이벤트 선택:
   - Pull request
   - Pull request review

### GitLab & Bitbucket 설정

GitLab과 Bitbucket 설정 방법은 [Phase 3 가이드](docs/PHASE3_GUIDE.md)를 참조하세요.

**지원 플랫폼:**
- ✅ GitHub (App & Personal Token)
- ✅ GitLab (Personal Token)
- ✅ Bitbucket (App Password)

## 프로젝트 구조

```
src/
├── main/
│   ├── java/com/codereview/assistant/
│   │   ├── config/          # 설정 클래스
│   │   ├── controller/      # REST 컨트롤러
│   │   ├── domain/          # JPA 엔티티
│   │   ├── repository/      # JPA 레포지토리
│   │   ├── service/         # 비즈니스 로직
│   │   ├── dto/             # DTO 클래스
│   │   └── util/            # 유틸리티
│   └── resources/
│       ├── application.yml  # 애플리케이션 설정
│       └── db/migration/    # 데이터베이스 마이그레이션
└── test/                    # 테스트 코드
```

## 개발 로드맵

### Phase 1 ✅ (완료)
- [x] 프로젝트 기본 구조 설정
- [x] GitHub Webhook 통합
- [x] AI 코드 분석 엔진
- [x] 자동 코멘트 생성

### Phase 2 ✅ (완료)
- [x] 대시보드 백엔드 API
- [x] 리뷰 통계 및 인사이트
- [x] 커스텀 리뷰 규칙 엔진
- [x] 단위 및 통합 테스트
- [x] 테스트 및 API 문서

### Phase 3 ✅ (완료)
- [x] GitLab/Bitbucket 지원
- [x] 멀티 언어 지원 확대 (Java, Python, JavaScript, TypeScript, Go, Rust, C++)

### Phase 4 (계획)
- [ ] 프론트엔드 대시보드 UI
- [ ] IDE 플러그인

## 라이선스

MIT License

## 기여하기

PR과 이슈는 언제나 환영합니다!

## 문의

이슈 탭에서 질문이나 버그를 제보해주세요.
