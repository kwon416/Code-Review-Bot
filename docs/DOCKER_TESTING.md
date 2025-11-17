# Docker 환경 테스트 가이드

Docker Compose를 사용하여 CodeReview AI Assistant를 로컬 환경에서 테스트하는 방법을 설명합니다.

## 사전 요구사항

- Docker (20.10 이상)
- Docker Compose (v2.0 이상)
- OpenAI API 키
- GitHub App 설정 (선택사항)

## 빠른 시작

### 1. 환경 변수 설정

테스트용 환경 변수 파일을 복사합니다:

```bash
cp .env.docker .env
```

`.env` 파일을 열어서 실제 값으로 수정합니다:

```bash
# 필수: OpenAI API 키
OPENAI_API_KEY=sk-your-actual-api-key

# 필수: 데이터베이스 자격증명 (원하는 값으로 변경 가능)
DB_USERNAME=codereview_user
DB_PASSWORD=your_secure_password

# 필수: Redis 비밀번호 (원하는 값으로 설정)
REDIS_PASSWORD=your_redis_password

# 필수: RabbitMQ 자격증명 (원하는 값으로 변경 가능)
RABBITMQ_USERNAME=codereview_admin
RABBITMQ_PASSWORD=your_rabbitmq_password

# 선택사항: GitHub App 설정 (웹훅 테스트 시 필요)
GITHUB_APP_ID=your_app_id
GITHUB_PRIVATE_KEY=your_private_key
GITHUB_WEBHOOK_SECRET=your_secret
```

### 2. Docker Compose 실행

모든 서비스를 시작합니다:

```bash
docker-compose up -d
```

빌드부터 시작하려면:

```bash
docker-compose up -d --build
```

### 3. 로그 확인

전체 로그 보기:

```bash
docker-compose logs -f
```

특정 서비스 로그만 보기:

```bash
docker-compose logs -f app
docker-compose logs -f postgres
docker-compose logs -f redis
docker-compose logs -f rabbitmq
```

### 4. 헬스 체크 확인

애플리케이션이 정상적으로 시작되었는지 확인:

```bash
curl http://localhost:8080/actuator/health
```

상세 헬스 정보 확인:

```bash
curl http://localhost:8080/actuator/health | jq
```

## 서비스 접근

### 애플리케이션 엔드포인트

- **메인 애플리케이션**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **Actuator Health**: http://localhost:8080/actuator/health
- **Prometheus Metrics**: http://localhost:8080/actuator/prometheus

### 인프라 서비스

- **PostgreSQL**: localhost:5432
  - Database: `codereview`
  - Username/Password: `.env` 파일에 설정한 값

- **Redis**: localhost:6379
  - Password: `.env` 파일에 설정한 값

- **RabbitMQ Management**: http://localhost:15672
  - Username/Password: `.env` 파일에 설정한 값

## API 테스트

### 1. 대시보드 통계 조회

```bash
curl http://localhost:8080/api/dashboard/statistics | jq
```

### 2. 최근 리뷰 조회

```bash
curl "http://localhost:8080/api/dashboard/reviews/recent?limit=5" | jq
```

### 3. 커스텀 규칙 목록 조회

```bash
curl http://localhost:8080/api/rules | jq
```

### 4. 웹훅 헬스 체크

```bash
curl http://localhost:8080/api/webhook/health
```

## 데이터베이스 접근

PostgreSQL에 직접 연결:

```bash
# 컨테이너 내부에서
docker exec -it codereview-postgres psql -U codereview_user -d codereview

# 또는 호스트에서
psql -h localhost -p 5432 -U codereview_user -d codereview
```

유용한 쿼리:

```sql
-- 모든 테이블 목록
\dt

-- 리뷰 규칙 확인
SELECT * FROM review_rules;

-- 최근 리뷰 확인
SELECT * FROM reviews ORDER BY created_at DESC LIMIT 5;
```

## 트러블슈팅

### 컨테이너가 시작되지 않는 경우

1. 로그 확인:
```bash
docker-compose logs app
```

2. 환경 변수가 제대로 설정되었는지 확인:
```bash
docker-compose config
```

3. 이전 볼륨 삭제 후 재시작:
```bash
docker-compose down -v
docker-compose up -d
```

### 데이터베이스 연결 실패

1. PostgreSQL이 정상 실행 중인지 확인:
```bash
docker-compose ps postgres
```

2. 헬스 체크 상태 확인:
```bash
docker inspect codereview-postgres | jq '.[0].State.Health'
```

3. PostgreSQL 로그 확인:
```bash
docker-compose logs postgres
```

### 메모리 부족 문제

Docker Desktop의 리소스 설정을 조정하세요:
- 최소 4GB RAM 할당 권장
- 2 CPU 코어 이상 권장

또는 `docker-compose.yml`에서 리소스 제한을 조정:

```yaml
deploy:
  resources:
    limits:
      cpus: '1.0'
      memory: 1G
```

## 개발 모드

코드 변경 사항을 실시간으로 반영하려면:

1. `docker-compose.yml`에 볼륨 마운트 추가:
```yaml
volumes:
  - ./src:/app/src
```

2. Spring Boot DevTools가 활성화되어 있는지 확인

3. 컨테이너 재시작:
```bash
docker-compose restart app
```

## 정리

### 서비스 중지

```bash
docker-compose stop
```

### 서비스 중지 및 컨테이너 삭제

```bash
docker-compose down
```

### 모든 데이터 삭제 (볼륨 포함)

```bash
docker-compose down -v
```

### 이미지까지 모두 삭제

```bash
docker-compose down -v --rmi all
```

## 프로덕션 배포

프로덕션 환경에서는:

1. **강력한 비밀번호 사용**: 모든 서비스에 강력하고 고유한 비밀번호 설정
2. **환경 변수 보안**: `.env` 파일을 절대 버전 관리에 포함하지 마세요
3. **리소스 모니터링**: Prometheus와 Grafana를 사용한 모니터링 설정
4. **로그 관리**: 중앙화된 로그 수집 시스템 사용
5. **백업**: 정기적인 데이터베이스 백업 설정
6. **SSL/TLS**: HTTPS 설정 필수

## 참고 자료

- [Docker Compose 문서](https://docs.docker.com/compose/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [PostgreSQL Docker](https://hub.docker.com/_/postgres)
- [Redis Docker](https://hub.docker.com/_/redis)
- [RabbitMQ Docker](https://hub.docker.com/_/rabbitmq)
