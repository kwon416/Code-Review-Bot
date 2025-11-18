# 기여 가이드

CodeReview AI Assistant 프로젝트에 기여해주셔서 감사합니다! 이 문서는 프로젝트에 기여하는 방법을 안내합니다.

## 목차
- [행동 강령](#행동-강령)
- [시작하기](#시작하기)
- [개발 워크플로우](#개발-워크플로우)
- [코드 스타일](#코드-스타일)
- [테스트](#테스트)
- [Pull Request 가이드라인](#pull-request-가이드라인)
- [이슈 리포팅](#이슈-리포팅)

## 행동 강령

### 우리의 약속

우리는 개방적이고 환영하는 환경을 조성하기 위해 다음을 약속합니다:

- 모든 기여자를 존중합니다
- 건설적인 피드백을 제공합니다
- 다양한 관점과 경험을 환영합니다

### 기대되는 행동

- 친절하고 포용적인 언어 사용
- 다른 관점과 경험 존중
- 건설적인 비판을 우아하게 수용
- 커뮤니티에 최선이 되는 것에 집중

## 시작하기

### 1. 저장소 포크

```bash
# GitHub에서 Fork 버튼 클릭
# 로컬에 클론
git clone https://github.com/YOUR_USERNAME/Code-Review-Bot.git
cd Code-Review-Bot
```

### 2. 개발 환경 설정

```bash
# 의존성 설치
./gradlew build

# 데이터베이스 시작
docker-compose up -d postgres redis rabbitmq

# 환경 변수 설정
cp .env.example .env
# .env 파일 수정
```

### 3. 원본 저장소 추가

```bash
git remote add upstream https://github.com/ORIGINAL_OWNER/Code-Review-Bot.git
```

## 개발 워크플로우

### 1. 브랜치 생성

```bash
# 최신 코드 가져오기
git checkout main
git pull upstream main

# 기능 브랜치 생성
git checkout -b feature/your-feature-name

# 또는 버그 수정
git checkout -b fix/your-bug-fix
```

### 브랜치 네이밍 규칙

- **기능 추가**: `feature/feature-name`
- **버그 수정**: `fix/bug-description`
- **문서 수정**: `docs/description`
- **리팩토링**: `refactor/description`
- **테스트**: `test/description`

### 2. 코드 작성

```bash
# 코드 수정
vim src/main/java/...

# 테스트 작성
vim src/test/java/...
```

### 3. 커밋

```bash
# 변경사항 추가
git add .

# 커밋 (명확한 메시지 작성)
git commit -m "feat: add new code review rule for Python"
```

### 커밋 메시지 규칙

**형식:**
```
<type>(<scope>): <subject>

<body>

<footer>
```

**타입:**
- `feat`: 새로운 기능
- `fix`: 버그 수정
- `docs`: 문서 수정
- `style`: 코드 포맷팅 (기능 변경 없음)
- `refactor`: 코드 리팩토링
- `test`: 테스트 추가/수정
- `chore`: 빌드 프로세스, 도구 변경

**예시:**
```
feat(webhook): add GitLab webhook support

- Add GitLabWebhookController
- Implement merge request event handling
- Add integration tests

Closes #123
```

### 4. 푸시

```bash
git push origin feature/your-feature-name
```

## 코드 스타일

### Java 코드 스타일

**Google Java Style Guide를 따릅니다.**

```java
// Good
public class CodeReviewService {
    private final ChatClient chatClient;

    public CodeReviewResult analyzeCode(String diff, String language) {
        // Implementation
    }
}

// Bad
public class CodeReviewService
{
    private ChatClient chatClient;

    public CodeReviewResult analyzeCode(String diff,String language)
    {
        // Implementation
    }
}
```

### 네이밍 컨벤션

- **클래스**: PascalCase (`CodeReviewService`)
- **메서드**: camelCase (`analyzeCode`)
- **상수**: UPPER_SNAKE_CASE (`MAX_RETRIES`)
- **패키지**: lowercase (`com.codereview.assistant`)

### Lombok 사용

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class SomeService {
    private final SomeDependency dependency;

    public void doSomething() {
        log.info("Doing something");
    }
}
```

### 주석

```java
/**
 * Analyzes code changes and returns review comments
 *
 * @param diffContent The git diff content
 * @param language Programming language
 * @return CodeReviewResult containing comments and summary
 */
public CodeReviewResult analyzeCode(String diffContent, String language) {
    // Implementation
}
```

## 테스트

### 테스트 작성 필수

모든 새로운 기능과 버그 수정에는 테스트가 필요합니다.

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("CodeReviewService 테스트")
class CodeReviewServiceTest {

    @Mock
    private ChatClient chatClient;

    @InjectMocks
    private CodeReviewService codeReviewService;

    @Test
    @DisplayName("코드 분석 성공")
    void analyzeCode_Success() {
        // Given
        String diffContent = "...";
        when(chatClient.call(any())).thenReturn(mockResponse);

        // When
        CodeReviewResult result = codeReviewService.analyzeCode(diffContent, "Java");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getComments()).hasSize(1);
    }
}
```

### 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 실행
./gradlew test --tests CodeReviewServiceTest

# 커버리지 리포트
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

### 테스트 커버리지

- **최소 커버리지**: 60%
- **목표 커버리지**: 80%

## Pull Request 가이드라인

### 1. PR 생성 전 체크리스트

- [ ] 최신 main 브랜치와 동기화
- [ ] 모든 테스트 통과
- [ ] 코드 스타일 준수
- [ ] 문서 업데이트 (필요시)
- [ ] CHANGELOG 업데이트 (필요시)

### 2. PR 생성

```bash
# GitHub에서 Pull Request 생성
# 템플릿에 따라 작성
```

### PR 템플릿

```markdown
## 변경 사항
<!-- 무엇을 변경했는지 설명 -->

## 변경 이유
<!-- 왜 이 변경이 필요한지 설명 -->

## 테스트 방법
<!-- 어떻게 테스트했는지 설명 -->

## 체크리스트
- [ ] 테스트 작성 및 통과
- [ ] 문서 업데이트
- [ ] 코드 스타일 준수
- [ ] 커밋 메시지 규칙 준수

## 스크린샷 (필요시)
<!-- UI 변경 시 스크린샷 첨부 -->

## 관련 이슈
Closes #(이슈 번호)
```

### 3. 코드 리뷰 대응

- 리뷰어의 피드백에 신속하게 대응
- 건설적인 토론 환영
- 필요시 코드 수정 및 재푸시

### 4. Merge

- 리뷰 승인 후 maintainer가 merge
- Squash merge 사용

## 이슈 리포팅

### 버그 리포트

```markdown
**버그 설명**
명확하고 간결한 버그 설명

**재현 방법**
1. '...'로 이동
2. '...' 클릭
3. '...'까지 스크롤
4. 에러 확인

**예상 동작**
무엇이 일어나야 하는지 설명

**실제 동작**
실제로 무엇이 일어났는지 설명

**스크린샷**
가능하면 스크린샷 첨부

**환경**
- OS: [e.g. Ubuntu 22.04]
- Java Version: [e.g. 17]
- Docker Version: [e.g. 24.0.5]

**추가 정보**
기타 관련 정보
```

### 기능 요청

```markdown
**기능이 문제를 해결하나요?**
현재 문제에 대한 명확한 설명

**원하는 솔루션**
어떤 기능을 원하는지 설명

**대안**
고려한 다른 대안들

**추가 정보**
기타 관련 정보, 스크린샷 등
```

## 개발 팁

### 로컬 디버깅

```bash
# IntelliJ IDEA
# Run > Edit Configurations
# Add New Configuration > Spring Boot
# Main class: CodeReviewAssistantApplication

# 환경 변수 설정
# Environment variables에 .env 내용 추가
```

### 데이터베이스 마이그레이션

```bash
# 새 마이그레이션 파일 생성
# src/main/resources/db/migration/V{version}__description.sql

# 예: V3__add_custom_rules.sql
```

### API 테스트

```bash
# Swagger UI 사용
http://localhost:8080/swagger-ui/index.html

# curl 사용
curl -X GET http://localhost:8080/api/dashboard/statistics | jq
```

## 질문이 있나요?

- 이슈 생성: [GitHub Issues](https://github.com/yourusername/Code-Review-Bot/issues)
- 이메일: your-email@example.com

## 감사합니다!

여러분의 기여가 프로젝트를 더 좋게 만듭니다. 🎉
