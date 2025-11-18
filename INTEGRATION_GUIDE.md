# 🔗 GitHub 실제 연동 가이드

로컬에서 실행하여 실제 GitHub PR에 자동 코드 리뷰가 달리도록 설정하는 방법입니다.

## 📋 사전 준비물

### 필수 항목
- ✅ **Java 17 이상**
- ✅ **OpenAI API Key** (필수!)
- ✅ **GitHub Personal Access Token** 또는 **GitHub App**
- ✅ **ngrok** 또는 **localtunnel** (Webhook 수신용)

### 선택 항목
- PostgreSQL (없으면 H2 사용)
- Redis (없으면 비활성화)
- RabbitMQ (없으면 비활성화)

---

## 🚀 빠른 시작 (5분 설정)

### Step 1: OpenAI API Key 발급

1. https://platform.openai.com/api-keys 접속
2. "Create new secret key" 클릭
3. 키 복사 (절대 잃어버리지 마세요!)

### Step 2: GitHub Personal Access Token 생성

1. GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. "Generate new token (classic)" 클릭
3. 권한 선택:
   - ✅ `repo` (전체 권한)
   - ✅ `write:discussion` (PR 코멘트 작성)
4. Generate token 클릭 및 복사

### Step 3: 환경 변수 설정

프로젝트 루트에 `.env` 파일 생성:

```bash
# .env 파일
OPENAI_API_KEY=sk-your-openai-api-key-here
GITHUB_TOKEN=ghp_your-github-token-here
```

### Step 4: ngrok 설치 및 실행

#### 방법 A: ngrok (권장)

```bash
# ngrok 설치
# macOS
brew install ngrok

# Linux
wget https://bin.equinox.io/c/bNyj1mQVY4c/ngrok-v3-stable-linux-amd64.tgz
tar xvzf ngrok-v3-stable-linux-amd64.tgz
sudo mv ngrok /usr/local/bin

# ngrok 계정 등록 (무료)
# https://dashboard.ngrok.com/signup
# Auth token 복사

# ngrok 인증
ngrok authtoken YOUR_AUTH_TOKEN

# 터널 시작 (8080 포트)
ngrok http 8080
```

실행 결과:
```
Session Status                online
Account                       your-account
Forwarding                    https://abcd-1234-5678.ngrok.io -> http://localhost:8080
```

⚠️ **중요**: `https://abcd-1234-5678.ngrok.io` URL을 복사하세요!

#### 방법 B: localtunnel (대안)

```bash
# localtunnel 설치
npm install -g localtunnel

# 터널 시작
lt --port 8080
```

### Step 5: 애플리케이션 실행

```bash
# 환경 변수와 함께 실행
OPENAI_API_KEY=sk-xxx GITHUB_TOKEN=ghp-xxx ./gradlew bootRun --args='--spring.profiles.active=local'
```

또는 `.env` 파일을 만들었다면:

```bash
# .env 파일 로드 후 실행
export $(cat .env | xargs)
./gradlew bootRun --args='--spring.profiles.active=local'
```

애플리케이션이 시작되면:
```
Started CodeReviewAssistantApplication in 15.234 seconds
```

### Step 6: GitHub Repository Webhook 설정

1. **테스트할 GitHub 저장소** 선택
2. **Settings → Webhooks → Add webhook** 클릭
3. 설정:
   - **Payload URL**: `https://your-ngrok-url.ngrok.io/api/webhook/github`
   - **Content type**: `application/json`
   - **Secret**: (비워두기 또는 원하는 시크릿)
   - **Which events**: "Let me select individual events"
     - ✅ Pull requests
   - ✅ Active
4. **Add webhook** 클릭

### Step 7: 테스트 PR 생성

```bash
# 테스트 저장소에서
git checkout -b test-code-review
echo "public class Test { private String password = \"12345\"; }" > Test.java
git add Test.java
git commit -m "Test: Add hardcoded password"
git push origin test-code-review

# GitHub에서 PR 생성
```

---

## 🎉 성공!

PR을 생성하면:
1. ⚡ Webhook이 로컬 서버로 전송됨
2. 🤖 AI가 코드 분석
3. 💬 자동으로 코멘트 작성!

---

## 🔍 문제 해결

### Webhook이 도착하지 않음

**증상**: PR을 만들었지만 아무 일도 일어나지 않음

**해결**:
1. ngrok이 실행 중인지 확인
2. 애플리케이션이 8080 포트에서 실행 중인지 확인
3. GitHub Webhook 페이지에서 "Recent Deliveries" 확인
   - ✅ 200 OK: 성공
   - ❌ 오류: 로그 확인

```bash
# 로컬 앱 로그 확인
# 터미널에서 확인 가능
```

### OpenAI API 오류

**증상**: `Error: Invalid API Key`

**해결**:
1. API 키가 올바른지 확인
2. OpenAI 계정에 크레딧이 있는지 확인
3. 환경 변수가 제대로 설정되었는지 확인:
```bash
echo $OPENAI_API_KEY
```

### GitHub API 권한 오류

**증상**: `403 Forbidden` 또는 코멘트 작성 실패

**해결**:
1. GitHub Token이 올바른지 확인
2. Token에 `repo` 권한이 있는지 확인
3. Private 저장소인 경우 추가 권한 필요

---

## 🏗️ 프로덕션 배포 (선택)

로컬 테스트가 성공했다면 실제 서버에 배포하세요:

### AWS EC2 배포

```bash
# 1. EC2 인스턴스 생성 (Ubuntu 22.04)
# 2. 보안 그룹 설정 (80, 443, 8080 포트 열기)
# 3. 서버 접속

ssh -i your-key.pem ubuntu@your-ec2-ip

# 4. Docker 설치
sudo apt update
sudo apt install -y docker.io docker-compose

# 5. 프로젝트 클론
git clone https://github.com/yourusername/Code-Review-Bot.git
cd Code-Review-Bot

# 6. 환경 변수 설정
sudo nano .env
# OPENAI_API_KEY, GITHUB_TOKEN 등 설정

# 7. Docker Compose 실행
sudo docker-compose up -d

# 8. GitHub Webhook URL 업데이트
# http://your-ec2-ip:8080/api/webhook/github
```

### Heroku 배포 (간단)

```bash
# Heroku CLI 설치
# https://devcenter.heroku.com/articles/heroku-cli

# 로그인
heroku login

# 앱 생성
heroku create your-app-name

# 환경 변수 설정
heroku config:set OPENAI_API_KEY=sk-xxx
heroku config:set GITHUB_TOKEN=ghp-xxx

# 배포
git push heroku main

# GitHub Webhook URL
# https://your-app-name.herokuapp.com/api/webhook/github
```

---

## 📊 동작 확인

### 1. Health Check
```bash
curl http://localhost:8080/actuator/health
```

### 2. Webhook 수신 확인
```bash
curl -X POST http://localhost:8080/api/webhook/github \
  -H "Content-Type: application/json" \
  -H "X-GitHub-Event: pull_request" \
  -d '{
    "action": "opened",
    "pull_request": {
      "number": 1,
      "title": "Test PR"
    }
  }'
```

### 3. 로그 모니터링
애플리케이션 실행 터미널에서:
```
Received GitHub webhook: pull_request
Processing PR #1 from testowner/testrepo
AI analysis started...
Review completed! Posted 3 comments.
```

---

## 💡 Pro Tips

### 1. 비용 절약
OpenAI API는 사용량에 따라 과금됩니다:
- gpt-4-turbo: 토큰당 비용이 높음
- gpt-3.5-turbo: 저렴한 대안

`application-local.yml`에서 모델 변경:
```yaml
spring:
  ai:
    openai:
      model: gpt-3.5-turbo  # 비용 절감
```

### 2. 특정 파일만 리뷰
대용량 PR은 비용이 많이 듭니다. 특정 파일만 리뷰하도록 설정 가능 (추후 기능)

### 3. 리뷰 규칙 커스터마이징
```bash
# 커스텀 규칙 추가
curl -X POST http://localhost:8080/api/rules \
  -H "Content-Type: application/json" \
  -d '{
    "name": "내 프로젝트 규칙",
    "ruleType": "custom_prompt",
    "ruleConfig": {"prompt": "Check for..."}
  }'
```

---

## 🎬 데모 시나리오

### 시나리오: 보안 취약점 발견

1. **PR 생성**:
```java
// UserController.java
public void login(String username, String password) {
    String sql = "SELECT * FROM users WHERE username='" + username + "'";
    // SQL Injection 취약점!
}
```

2. **AI 코멘트 자동 작성**:
```
⚠️ Security Issue: SQL Injection vulnerability detected

이 코드는 SQL Injection 공격에 취약합니다.

권장 해결책:
- PreparedStatement 사용
- 또는 JPA/Hibernate 사용

수정 예시:
```java
String sql = "SELECT * FROM users WHERE username = ?";
PreparedStatement pstmt = conn.prepareStatement(sql);
pstmt.setString(1, username);
```
```

---

## 📚 참고 자료

- [GitHub Webhooks 문서](https://docs.github.com/en/developers/webhooks-and-events/webhooks)
- [OpenAI API 문서](https://platform.openai.com/docs)
- [ngrok 문서](https://ngrok.com/docs)

---

## 🆘 도움이 필요하신가요?

1. GitHub Issues에 문의
2. 로그 파일 첨부
3. Webhook 전송 기록 첨부 (GitHub Settings → Webhooks → Recent Deliveries)

---

**이제 실제 코드 리뷰를 경험해보세요! 🚀**
