# CodeReview AI Assistant

AI 기반 실시간 코드 리뷰 자동화 시스템

## 프로젝트 개요

개인 개발자와 소규모 팀을 위한 AI 기반 자동 코드 리뷰 시스템입니다. GitHub PR 생성 시 자동으로 코드를 분석하고, 품질 개선 사항을 제안합니다.

## 주요 기능

- GitHub Webhook 연동을 통한 실시간 PR 감지
- AI 기반 코드 분석 (버그, 성능, 보안, 베스트 프랙티스)
- 자동 GitHub 코멘트 생성
- 리뷰 히스토리 및 통계 대시보드

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
# OpenAI API 키 설정
OPENAI_API_KEY=your_openai_api_key

# GitHub App 설정
GITHUB_APP_ID=your_app_id
GITHUB_PRIVATE_KEY=your_private_key
GITHUB_WEBHOOK_SECRET=your_webhook_secret
```

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

## API 엔드포인트

- `POST /api/webhook/github` - GitHub Webhook 수신
- `GET /api/webhook/health` - Health Check
- `GET /actuator/health` - Actuator Health Check

## GitHub App 설정 가이드

1. GitHub에서 새 GitHub App 생성
2. Webhook URL 설정: `https://your-domain.com/api/webhook/github`
3. 필요한 권한 설정:
   - Repository permissions:
     - Contents: Read
     - Pull requests: Read & Write
4. Webhook 이벤트 선택:
   - Pull request
   - Pull request review

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

### Phase 1 (현재)
- [x] 프로젝트 기본 구조 설정
- [ ] GitHub Webhook 통합
- [ ] AI 코드 분석 엔진
- [ ] 자동 코멘트 생성

### Phase 2
- [ ] 대시보드 UI
- [ ] 리뷰 통계 및 인사이트
- [ ] 커스텀 리뷰 규칙

### Phase 3
- [ ] GitLab/Bitbucket 지원
- [ ] IDE 플러그인
- [ ] 멀티 언어 지원 확대

## 라이선스

MIT License

## 기여하기

PR과 이슈는 언제나 환영합니다!

## 문의

이슈 탭에서 질문이나 버그를 제보해주세요.
