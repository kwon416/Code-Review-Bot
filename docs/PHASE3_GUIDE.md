# Phase 3: GitLab/Bitbucket 지원 및 다국어 확장

Phase 3에서는 GitHub 외에 GitLab과 Bitbucket을 지원하고, 다양한 프로그래밍 언어에 특화된 코드 리뷰 기능을 추가했습니다.

## 주요 기능

### 1. 멀티 플랫폼 지원

#### GitHub (기존)
- GitHub App 또는 Personal Access Token 인증
- Pull Request 웹훅 지원
- 인라인 코멘트 및 요약 코멘트

#### GitLab (신규)
- Personal Access Token 인증
- Merge Request 웹훅 지원
- Discussion API를 통한 인라인 코멘트
- GitLab.com 및 Self-hosted GitLab 지원

#### Bitbucket (신규)
- App Password 인증
- Pull Request 웹훅 지원
- 인라인 코멘트 지원
- Bitbucket Cloud API v2.0 사용

### 2. 언어별 특화 코드 리뷰

다음 언어들에 대해 특화된 리뷰 가이드라인을 제공합니다:

- **Java**: Spring Framework, 예외 처리, 스레드 안전성, NullPointerException 방지
- **Python**: PEP 8, Type hints, 비동기 프로그래밍, 보안 이슈
- **JavaScript**: ES6+, 비동기 처리, XSS 방지, 메모리 누수
- **TypeScript**: 타입 안전성, 제네릭, Utility Types
- **Go**: 에러 처리, Goroutine 관리, 채널 사용, Context
- **Rust**: 소유권/대여, unsafe 블록, 에러 전파, 타입 시스템
- **C++**: 메모리 관리, 스마트 포인터, RAII, 이동 의미론

## 설정 가이드

### GitLab 설정

#### 1. Personal Access Token 생성

1. GitLab → Settings → Access Tokens
2. Token 이름 입력 (예: "Code Review Bot")
3. 스코프 선택:
   - `api`: API 접근
   - `read_repository`: 저장소 읽기
   - `write_repository`: 코멘트 작성
4. Create token 클릭 및 토큰 저장

#### 2. 환경 변수 설정

```bash
# .env 파일
GITLAB_API_URL=https://gitlab.com/api/v4  # Self-hosted의 경우 URL 변경
GITLAB_TOKEN=your_gitlab_personal_access_token
```

#### 3. GitLab Webhook 설정

1. 프로젝트 → Settings → Webhooks
2. URL 입력: `https://your-domain.com/api/webhook/gitlab`
3. Trigger 선택:
   - ✓ Merge request events
4. Add webhook 클릭

### Bitbucket 설정

#### 1. App Password 생성

1. Bitbucket → Personal settings → App passwords
2. Create app password 클릭
3. Label 입력 (예: "Code Review Bot")
4. Permissions 선택:
   - Repositories: Read, Write
   - Pull requests: Read, Write
5. Create 클릭 및 비밀번호 저장

#### 2. 환경 변수 설정

```bash
# .env 파일
BITBUCKET_API_URL=https://api.bitbucket.org/2.0
BITBUCKET_USERNAME=your_bitbucket_username
BITBUCKET_APP_PASSWORD=your_bitbucket_app_password
```

#### 3. Bitbucket Webhook 설정

1. Repository → Settings → Webhooks
2. Add webhook 클릭
3. Title 입력: "Code Review Bot"
4. URL 입력: `https://your-domain.com/api/webhook/bitbucket`
5. Triggers 선택:
   - ✓ Pull Request → Created
   - ✓ Pull Request → Updated
6. Save 클릭

## API 엔드포인트

### GitLab Webhook

```
POST /api/webhook/gitlab
GET  /api/webhook/gitlab/health
```

**Headers:**
- `X-Gitlab-Event`: Merge Request Hook
- `X-Gitlab-Token`: (선택) Webhook secret token

**Request Body:**
```json
{
  "object_kind": "merge_request",
  "project": {
    "id": 123,
    "path_with_namespace": "user/repo"
  },
  "object_attributes": {
    "iid": 1,
    "title": "Feature: Add new feature",
    "state": "opened",
    "action": "open"
  }
}
```

### Bitbucket Webhook

```
POST /api/webhook/bitbucket
GET  /api/webhook/bitbucket/health
```

**Headers:**
- `X-Event-Key`: pullrequest:created, pullrequest:updated
- `X-Request-UUID`: Request identifier

**Request Body:**
```json
{
  "pullrequest": {
    "id": 1,
    "title": "Feature: Add new feature",
    "state": "OPEN"
  },
  "repository": {
    "full_name": "user/repo"
  }
}
```

## 사용 예시

### 1. GitHub에서 사용

```bash
# Pull Request 생성
git checkout -b feature/new-feature
git commit -m "Add new feature"
git push origin feature/new-feature
# GitHub UI에서 PR 생성
# → 자동으로 코드 리뷰 시작
```

### 2. GitLab에서 사용

```bash
# Merge Request 생성
git checkout -b feature/new-feature
git commit -m "Add new feature"
git push origin feature/new-feature
# GitLab UI에서 MR 생성
# → 자동으로 코드 리뷰 시작
```

### 3. Bitbucket에서 사용

```bash
# Pull Request 생성
git checkout -b feature/new-feature
git commit -m "Add new feature"
git push origin feature/new-feature
# Bitbucket UI에서 PR 생성
# → 자동으로 코드 리뷰 시작
```

## 언어 감지

### 자동 감지

시스템은 다음 방법으로 언어를 자동 감지합니다:

1. **GitLab**: Repository language 필드 사용
2. **Bitbucket**: Repository language 필드 사용
3. **GitHub**: Repository language API 사용
4. **Fallback**: 프로젝트/레포지토리 이름에서 추론

### 수동 지정

API를 직접 호출하는 경우 언어를 명시할 수 있습니다:

```java
CodeReviewResult result = codeReviewService.analyzeCode(diffContent, "Java");
```

## 지원 언어 목록

현재 특화 지원하는 언어:

- Java
- Python
- JavaScript
- TypeScript
- Go
- Rust
- C++

기타 언어는 일반 코드 리뷰 가이드라인으로 처리됩니다.

## 트러블슈팅

### GitLab

**문제**: "Failed to fetch MR diff"
- **해결**: GITLAB_TOKEN 권한 확인 (api, read_repository 필요)

**문제**: 인라인 코멘트가 작성되지 않음
- **해결**: GitLab API Discussion 권한 확인, fallback으로 일반 코멘트 작성됨

### Bitbucket

**문제**: "Failed to authenticate"
- **해결**: App Password 권한 확인 (Repositories: Write, Pull requests: Write 필요)

**문제**: Diff를 가져올 수 없음
- **해결**: Repository 접근 권한 확인

### 공통

**문제**: 웹훅이 트리거되지 않음
- **해결**:
  1. Webhook URL이 올바른지 확인
  2. 서버가 외부에서 접근 가능한지 확인
  3. 웹훅 이벤트 설정 확인

**문제**: 코드 리뷰가 실행되지 않음
- **해결**:
  1. 로그 확인: `docker-compose logs -f app`
  2. OpenAI API 키 확인
  3. 플랫폼별 토큰/인증 정보 확인

## 성능 최적화

### 비동기 처리

모든 웹훅은 비동기로 처리되어 웹훅 응답 시간을 최소화합니다:

```java
new Thread(() -> {
    try {
        gitLabWebhookService.handleMergeRequestEvent(event);
    } catch (Exception e) {
        log.error("Error handling GitLab MR event", e);
    }
}).start();
```

향후 개선: Spring Async 또는 RabbitMQ를 통한 메시지 큐 처리

### 캐싱

- Redis를 활용한 API 응답 캐싱 (예정)
- 이미 리뷰한 커밋은 재리뷰하지 않음 (예정)

## 보안 고려사항

### 토큰 관리

- 모든 API 토큰은 환경 변수로 관리
- `.env` 파일을 Git에 커밋하지 않음 (`.gitignore`에 포함)
- 프로덕션 환경에서는 Secret Manager 사용 권장

### Webhook 보안

- GitLab: X-Gitlab-Token 헤더로 검증 (선택)
- Bitbucket: IP whitelist 설정 권장
- GitHub: Webhook secret 사용

### API 권한 최소화

각 플랫폼에서 필요한 최소 권한만 부여:

- **GitLab**: api, read_repository, write_repository
- **Bitbucket**: Repositories (Read, Write), Pull requests (Read, Write)
- **GitHub**: Contents (Read), Pull requests (Read & Write)

## 다음 단계

Phase 3 완료 후 추가 개선 사항:

1. **IDE 플러그인 개발**
   - IntelliJ IDEA 플러그인
   - VSCode 확장

2. **추가 언어 지원**
   - C#, Ruby, PHP, Swift, Kotlin 등

3. **고급 분석 기능**
   - 코드 중복 감지
   - 복잡도 분석
   - 테스트 커버리지 체크

4. **성능 개선**
   - RabbitMQ 큐 활용
   - Redis 캐싱 강화
   - 병렬 처리 최적화
