# 🚀 로컬 실행 가이드

Docker 없이 로컬 환경에서 CodeReview AI Assistant를 실행하는 방법입니다.

## 📋 사전 요구사항

- **Java 17 이상**
- **인터넷 연결** (최초 빌드 시 의존성 다운로드)

## 🎯 빠른 시작

### 방법 1: 스크립트 실행 (권장)

```bash
# 실행 권한 부여 (최초 1회)
chmod +x run-local.sh

# 애플리케이션 실행
./run-local.sh
```

### 방법 2: Gradle 직접 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 방법 3: IDE에서 실행 (IntelliJ IDEA / Eclipse)

1. **메인 클래스**: `CodeReviewAssistantApplication`
2. **VM Options**: `-Dspring.profiles.active=local`
3. **Run** 클릭

## 🗄️ 로컬 프로파일 특징

### H2 인메모리 데이터베이스
- PostgreSQL 호환 모드로 실행
- 애플리케이션 종료 시 데이터 초기화
- 시작할 때마다 샘플 데이터 자동 로드

### 외부 의존성 제거
- ✅ PostgreSQL → H2 (인메모리)
- ✅ Redis → 비활성화
- ✅ RabbitMQ → 선택적

### 샘플 데이터
- 3개 Repository
- 5개 Pull Request
- 5개 Review
- 18개 Comment
- 3개 Review Rule

## 🌐 접속 정보

### 애플리케이션이 시작되면 다음 주소로 접속 가능합니다:

| 서비스 | URL | 설명 |
|--------|-----|------|
| **메인 대시보드** | http://localhost:8080 | 프론트엔드 UI |
| **API 문서** | http://localhost:8080/swagger-ui/index.html | Swagger UI |
| **Health Check** | http://localhost:8080/actuator/health | 상태 확인 |
| **H2 Console** | http://localhost:8080/h2-console | 데이터베이스 콘솔 |
| **Metrics** | http://localhost:8080/actuator/metrics | 애플리케이션 메트릭 |

### H2 Console 접속 정보
```
JDBC URL: jdbc:h2:mem:codereview
Username: sa
Password: (비워두기)
```

## 📊 대시보드 미리보기

### 1. 메인 대시보드
- **전체 통계**: Repository, 리뷰, 코멘트 수
- **차트**: 심각도별/카테고리별 이슈 분포
- **최근 활동**: 오늘/이번 주/이번 달 리뷰 수
- **최근 리뷰 목록**: 실시간 업데이트

### 2. API 엔드포인트

#### 통계 조회
```bash
curl http://localhost:8080/api/dashboard/statistics
```

#### 최근 리뷰 목록
```bash
curl http://localhost:8080/api/dashboard/reviews/recent?limit=10
```

#### 트렌드 데이터
```bash
curl http://localhost:8080/api/dashboard/trends?days=30
```

#### 리뷰 규칙 목록
```bash
curl http://localhost:8080/api/rules
```

## 🔧 트러블슈팅

### "java: command not found"
```bash
# macOS (Homebrew)
brew install openjdk@17

# Ubuntu/Debian
sudo apt install openjdk-17-jdk

# Windows
# Oracle JDK 또는 OpenJDK 다운로드 및 설치
```

### "Port 8080 already in use"
```bash
# 포트 사용 중인 프로세스 확인
lsof -i :8080

# 프로세스 종료
kill -9 <PID>

# 또는 다른 포트 사용
./gradlew bootRun --args='--spring.profiles.active=local --server.port=8081'
```

### 빌드 오류
```bash
# Gradle 캐시 정리
./gradlew clean

# 다시 빌드
./gradlew build --refresh-dependencies
```

### H2 데이터베이스 초기화 실패
```bash
# data-local.sql 파일 확인
cat src/main/resources/data-local.sql

# 로그 확인
./gradlew bootRun --args='--spring.profiles.active=local --debug'
```

## 🎨 데모 시나리오

### 1. 대시보드 확인
1. http://localhost:8080 접속
2. 샘플 데이터로 채워진 통계 확인
3. 차트에서 이슈 분포 확인

### 2. API 테스트
```bash
# 통계 조회
curl http://localhost:8080/api/dashboard/statistics | jq

# 최근 리뷰
curl http://localhost:8080/api/dashboard/reviews/recent | jq

# Health Check
curl http://localhost:8080/actuator/health
```

### 3. H2 Console에서 데이터 확인
1. http://localhost:8080/h2-console 접속
2. JDBC URL: `jdbc:h2:mem:codereview` 입력
3. Connect 클릭
4. SQL 쿼리 실행:
```sql
-- 모든 리뷰 조회
SELECT * FROM REVIEWS;

-- 리뷰별 코멘트 수
SELECT r.id, r.review_status, COUNT(c.id) as comment_count
FROM REVIEWS r
LEFT JOIN COMMENTS c ON r.id = c.review_id
GROUP BY r.id;

-- 심각도별 통계
SELECT severity, COUNT(*) as count
FROM COMMENTS
GROUP BY severity;
```

## 💡 개발 팁

### 로그 레벨 변경
`application-local.yml`에서 로그 레벨 조정:
```yaml
logging:
  level:
    com.codereview.assistant: DEBUG  # TRACE, DEBUG, INFO, WARN, ERROR
```

### 샘플 데이터 수정
`src/main/resources/data-local.sql` 파일 편집 후 재시작

### 프론트엔드 수정
- HTML: `src/main/resources/templates/dashboard.html`
- CSS: `src/main/resources/static/css/dashboard.css`
- JS: `src/main/resources/static/js/dashboard.js`

변경 후 브라우저 새로고침 (Ctrl+F5)

## 🚀 프로덕션 배포

로컬 테스트 후 프로덕션 배포는 다음 문서를 참고하세요:
- [배포 가이드](docs/DEPLOYMENT.md)
- [Docker 배포](README.md#docker로-실행)

## 📖 추가 문서

- [README.md](README.md) - 프로젝트 개요
- [API 테스트 가이드](docs/API_TESTING.md) - API 상세 테스트 방법
- [테스트 가이드](docs/TESTING.md) - 단위/통합 테스트
- [기여 가이드](docs/CONTRIBUTING.md) - 개발 참여 방법

## ❓ 문제가 있나요?

1. [GitHub Issues](https://github.com/yourusername/Code-Review-Bot/issues)에 문의
2. 로그 파일 첨부
3. 실행 환경 정보 제공 (OS, Java 버전 등)

---

**즐거운 코딩하세요! 🎉**
