# 테스트 가이드

이 문서는 CodeReview AI Assistant 프로젝트의 테스트 방법을 설명합니다.

## 목차
- [테스트 구조](#테스트-구조)
- [단위 테스트 실행](#단위-테스트-실행)
- [통합 테스트 실행](#통합-테스트-실행)
- [테스트 커버리지](#테스트-커버리지)
- [테스트 작성 가이드](#테스트-작성-가이드)

## 테스트 구조

```
src/test/java/com/codereview/assistant/
├── service/              # 서비스 계층 단위 테스트
│   ├── StatisticsServiceTest.java
│   └── ReviewRuleServiceTest.java
├── controller/           # 컨트롤러 통합 테스트
│   └── DashboardControllerTest.java
└── repository/           # 리포지토리 테스트
```

## 단위 테스트 실행

### 전체 테스트 실행
```bash
./gradlew test
```

### 특정 테스트 클래스 실행
```bash
./gradlew test --tests StatisticsServiceTest
```

### 특정 테스트 메서드 실행
```bash
./gradlew test --tests StatisticsServiceTest.getDashboardStatistics_Success
```

### 테스트 결과 확인
```bash
# 테스트 리포트 생성
./gradlew test

# 리포트 확인
open build/reports/tests/test/index.html  # macOS
xdg-open build/reports/tests/test/index.html  # Linux
```

## 통합 테스트 실행

### Docker 환경에서 테스트

1. **의존성 서비스 시작**
```bash
docker-compose up -d postgres redis rabbitmq
```

2. **통합 테스트 실행**
```bash
./gradlew integrationTest
```

3. **서비스 정리**
```bash
docker-compose down
```

### 테스트 프로파일 사용

```bash
# test 프로파일로 애플리케이션 실행
./gradlew bootRun --args='--spring.profiles.active=test'
```

## 테스트 커버리지

### JaCoCo 플러그인 사용

1. **커버리지 리포트 생성**
```bash
./gradlew test jacocoTestReport
```

2. **커버리지 확인**
```bash
# HTML 리포트
open build/reports/jacoco/test/html/index.html

# 최소 커버리지 검증
./gradlew jacocoTestCoverageVerification
```

### 현재 커버리지 목표
- **라인 커버리지**: 70% 이상
- **브랜치 커버리지**: 60% 이상

## 테스트 작성 가이드

### 네이밍 컨벤션

**테스트 클래스명**
```
{테스트 대상 클래스명}Test
예: StatisticsServiceTest
```

**테스트 메서드명**
```
{메서드명}_{시나리오}
예: getDashboardStatistics_Success
    getDashboardStatistics_WhenNoData
```

### 테스트 구조 (Given-When-Then)

```java
@Test
@DisplayName("대시보드 전체 통계 조회 성공")
void getDashboardStatistics_Success() {
    // Given - 테스트 준비
    when(repositoryRepository.count()).thenReturn(5L);

    // When - 테스트 실행
    DashboardStatistics result = statisticsService.getDashboardStatistics();

    // Then - 검증
    assertThat(result).isNotNull();
    assertThat(result.getOverallStats().getTotalRepositories()).isEqualTo(5L);

    verify(repositoryRepository).count();
}
```

### Mocking 가이드

**@Mock vs @MockBean**
- `@Mock`: 단위 테스트용 (Mockito)
- `@MockBean`: 통합 테스트용 (Spring Boot)

```java
// 단위 테스트
@ExtendWith(MockitoExtension.class)
class ServiceTest {
    @Mock
    private SomeRepository repository;

    @InjectMocks
    private SomeService service;
}

// 통합 테스트
@WebMvcTest(SomeController.class)
class ControllerTest {
    @MockBean
    private SomeService service;

    @Autowired
    private MockMvc mockMvc;
}
```

### Assertion 라이브러리 사용

**AssertJ 사용 권장**
```java
// AssertJ (권장)
assertThat(result).isNotNull();
assertThat(list).hasSize(5);
assertThat(value).isEqualTo(expected);

// JUnit (기본)
assertNotNull(result);
assertEquals(5, list.size());
```

## 테스트 데이터 준비

### @BeforeEach 사용

```java
@BeforeEach
void setUp() {
    testRepository = Repository.builder()
        .id(1L)
        .githubId(12345L)
        .owner("testowner")
        .name("testrepo")
        .build();
}
```

### Test Fixtures 파일 활용

```java
// src/test/resources/fixtures/
review-sample.json
comment-sample.json
```

## 실제 테스트 예제

### 서비스 계층 테스트

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("StatisticsService 테스트")
class StatisticsServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    @Test
    @DisplayName("최근 리뷰 목록 조회 성공")
    void getRecentReviews_Success() {
        // Given
        int limit = 10;
        Page<Review> reviewPage = new PageImpl<>(List.of(testReview));
        when(reviewRepository.findAll(any(Pageable.class)))
            .thenReturn(reviewPage);

        // When
        List<ReviewSummaryDto> result =
            statisticsService.getRecentReviews(limit);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        verify(reviewRepository).findAll(any(Pageable.class));
    }
}
```

### 컨트롤러 통합 테스트

```java
@WebMvcTest(DashboardController.class)
@DisplayName("DashboardController 통합 테스트")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatisticsService statisticsService;

    @Test
    @DisplayName("GET /api/dashboard/statistics - 성공")
    void getDashboardStatistics_Success() throws Exception {
        // Given
        DashboardStatistics statistics = // ... 준비
        when(statisticsService.getDashboardStatistics())
            .thenReturn(statistics);

        // When & Then
        mockMvc.perform(get("/api/dashboard/statistics")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.overallStats.totalRepositories")
                .value(5));
    }
}
```

## CI/CD 통합

### GitHub Actions 예제

```yaml
name: Test

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:15-alpine
        env:
          POSTGRES_DB: codereview_test
          POSTGRES_PASSWORD: postgres
        ports:
          - 5432:5432

      redis:
        image: redis:7-alpine
        ports:
          - 6379:6379

    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'

      - name: Run tests
        run: ./gradlew test

      - name: Generate coverage report
        run: ./gradlew jacocoTestReport

      - name: Upload coverage
        uses: codecov/codecov-action@v3
```

## 문제 해결

### 일반적인 문제

**1. 테스트가 실패하는 경우**
```bash
# 자세한 로그 확인
./gradlew test --info

# 특정 테스트만 디버깅
./gradlew test --tests TestClass --debug
```

**2. Mock이 작동하지 않는 경우**
- `@ExtendWith(MockitoExtension.class)` 확인
- Mock 객체 초기화 확인
- `when().thenReturn()` 설정 확인

**3. 데이터베이스 연결 실패**
```bash
# Docker 서비스 확인
docker-compose ps

# 포트 충돌 확인
lsof -i :5432
```

## 참고 자료

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
