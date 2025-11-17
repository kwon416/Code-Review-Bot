# 배포 가이드

이 문서는 CodeReview AI Assistant를 프로덕션 환경에 배포하는 방법을 설명합니다.

## 목차
- [사전 요구사항](#사전-요구사항)
- [Docker를 이용한 배포](#docker를-이용한-배포)
- [AWS 배포](#aws-배포)
- [환경 변수 설정](#환경-변수-설정)
- [보안 설정](#보안-설정)
- [모니터링](#모니터링)
- [트러블슈팅](#트러블슈팅)

## 사전 요구사항

### 필수 항목
- Docker & Docker Compose
- PostgreSQL 15+
- Redis 7+
- RabbitMQ 3+
- OpenAI API Key
- GitHub/GitLab/Bitbucket 접근 토큰

### 권장 사양
- **CPU**: 2 vCPU 이상
- **메모리**: 4GB RAM 이상
- **디스크**: 20GB 이상
- **네트워크**: 안정적인 인터넷 연결

## Docker를 이용한 배포

### 1. 프로젝트 클론

```bash
git clone https://github.com/yourusername/Code-Review-Bot.git
cd Code-Review-Bot
```

### 2. 환경 변수 설정

```bash
cp .env.example .env
vim .env  # 환경 변수 수정
```

**필수 환경 변수:**
```bash
# Database
DB_USERNAME=postgres
DB_PASSWORD=your_strong_password

# Redis
REDIS_PASSWORD=your_redis_password

# RabbitMQ
RABBITMQ_USERNAME=admin
RABBITMQ_PASSWORD=your_rabbitmq_password

# OpenAI
OPENAI_API_KEY=sk-...

# GitHub (선택)
GITHUB_APP_ID=your_app_id
GITHUB_PRIVATE_KEY=your_private_key
GITHUB_WEBHOOK_SECRET=your_webhook_secret

# GitLab (선택)
GITLAB_TOKEN=your_gitlab_token

# Bitbucket (선택)
BITBUCKET_USERNAME=your_username
BITBUCKET_APP_PASSWORD=your_app_password
```

### 3. Docker Compose로 실행

```bash
# 모든 서비스 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f app

# 상태 확인
docker-compose ps
```

### 4. 헬스 체크

```bash
# Health check
curl http://localhost:8080/actuator/health

# API 테스트
curl http://localhost:8080/api/webhook/health
```

## AWS 배포

### Option 1: EC2 인스턴스

#### 1. EC2 인스턴스 생성
- **인스턴스 타입**: t3.medium 이상
- **OS**: Ubuntu 22.04 LTS
- **스토리지**: 20GB 이상

#### 2. 보안 그룹 설정
```
인바운드 규칙:
- HTTP (80): 0.0.0.0/0
- HTTPS (443): 0.0.0.0/0
- SSH (22): Your IP
- Custom TCP (8080): 0.0.0.0/0 (개발 환경)
```

#### 3. 서버 설정

```bash
# 1. SSH 접속
ssh -i your-key.pem ubuntu@your-ec2-ip

# 2. Docker 설치
sudo apt-get update
sudo apt-get install -y docker.io docker-compose
sudo usermod -aG docker ubuntu

# 3. 프로젝트 배포
git clone https://github.com/yourusername/Code-Review-Bot.git
cd Code-Review-Bot

# 4. 환경 변수 설정
cp .env.example .env
nano .env  # 환경 변수 수정

# 5. 서비스 시작
docker-compose up -d
```

#### 4. Nginx 리버스 프록시 (선택)

```bash
# Nginx 설치
sudo apt-get install -y nginx

# 설정 파일 생성
sudo nano /etc/nginx/sites-available/codereview
```

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

```bash
# 설정 활성화
sudo ln -s /etc/nginx/sites-available/codereview /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

#### 5. SSL 인증서 (Let's Encrypt)

```bash
# Certbot 설치
sudo apt-get install -y certbot python3-certbot-nginx

# 인증서 발급
sudo certbot --nginx -d your-domain.com

# 자동 갱신 설정
sudo certbot renew --dry-run
```

### Option 2: AWS Elastic Beanstalk

#### 1. EB CLI 설치

```bash
pip install awsebcli
```

#### 2. Elastic Beanstalk 초기화

```bash
eb init -p docker code-review-assistant

# 지역 선택
# 애플리케이션 이름 입력
```

#### 3. 환경 생성 및 배포

```bash
# 환경 생성
eb create production-env

# 환경 변수 설정
eb setenv DB_USERNAME=postgres DB_PASSWORD=your_password \
  OPENAI_API_KEY=sk-...

# 배포
eb deploy

# 상태 확인
eb status

# 로그 확인
eb logs
```

#### 4. RDS 및 ElastiCache 연결

```bash
# RDS 엔드포인트 설정
eb setenv DB_URL=jdbc:postgresql://your-rds-endpoint:5432/codereview

# ElastiCache 설정
eb setenv REDIS_HOST=your-elasticache-endpoint
```

## 환경 변수 설정

### AWS Secrets Manager 사용

```bash
# AWS CLI 설치
pip install awscli

# Secret 생성
aws secretsmanager create-secret \
  --name codereview/production \
  --secret-string '{
    "OPENAI_API_KEY": "sk-...",
    "DB_PASSWORD": "...",
    "REDIS_PASSWORD": "..."
  }'
```

## 보안 설정

### 1. 강력한 비밀번호 사용

```bash
# 랜덤 비밀번호 생성
openssl rand -base64 32
```

### 2. 방화벽 설정

```bash
# UFW 활성화
sudo ufw enable

# 필요한 포트만 허용
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS

# 상태 확인
sudo ufw status
```

### 3. 로그 보안

```bash
# 민감한 정보 로깅 비활성화
# application.yml
logging:
  level:
    root: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

### 4. HTTPS 강제

Nginx 설정에서:
```nginx
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl;
    # SSL 설정...
}
```

## 모니터링

### 1. Prometheus & Grafana

```bash
# docker-compose.yml에 추가
prometheus:
  image: prom/prometheus
  ports:
    - "9090:9090"
  volumes:
    - ./prometheus.yml:/etc/prometheus/prometheus.yml

grafana:
  image: grafana/grafana
  ports:
    - "3000:3000"
```

### 2. Application Metrics

애플리케이션은 `/actuator/prometheus` 엔드포인트를 통해 메트릭을 노출합니다:

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'spring-boot'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['app:8080']
```

### 3. Health Checks

```bash
# Kubernetes liveness probe
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10

# Readiness probe
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
```

## 백업 및 복구

### 데이터베이스 백업

```bash
# 백업
docker exec postgres pg_dump -U postgres codereview > backup.sql

# 복구
docker exec -i postgres psql -U postgres codereview < backup.sql

# 자동 백업 스크립트
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
docker exec postgres pg_dump -U postgres codereview | gzip > backup_$DATE.sql.gz
```

### 환경 변수 백업

```bash
# .env 파일 암호화
gpg -c .env

# 복호화
gpg -d .env.gpg > .env
```

## 스케일링

### 수평 스케일링 (Horizontal Scaling)

```bash
# Docker Compose
docker-compose up -d --scale app=3

# Load balancer 설정 (Nginx)
upstream backend {
    server app1:8080;
    server app2:8080;
    server app3:8080;
}
```

### 수직 스케일링 (Vertical Scaling)

```yaml
# docker-compose.yml
services:
  app:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 4G
        reservations:
          cpus: '1'
          memory: 2G
```

## 트러블슈팅

### 컨테이너가 시작하지 않을 때

```bash
# 로그 확인
docker-compose logs app

# 컨테이너 상태 확인
docker-compose ps

# 재시작
docker-compose restart app
```

### 데이터베이스 연결 오류

```bash
# PostgreSQL 연결 테스트
docker exec -it postgres psql -U postgres -c "SELECT 1;"

# 네트워크 확인
docker network ls
docker network inspect code-review-bot_default
```

### 메모리 부족

```bash
# 메모리 사용량 확인
docker stats

# 컨테이너 메모리 제한 증가
docker-compose.yml에서 memory 설정 조정
```

## 참고 자료

- [Spring Boot Production Ready](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [AWS Well-Architected Framework](https://aws.amazon.com/architecture/well-architected/)
