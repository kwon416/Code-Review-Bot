-- 샘플 Repository 데이터
INSERT INTO repositories (github_id, owner, name, installation_id, created_at)
VALUES
  (12345, 'testuser', 'demo-project', 1, CURRENT_TIMESTAMP),
  (12346, 'testuser', 'backend-api', 1, CURRENT_TIMESTAMP),
  (12347, 'testuser', 'frontend-app', 1, CURRENT_TIMESTAMP);

-- 샘플 Pull Request 데이터
INSERT INTO pull_requests (repository_id, pr_number, title, description, author, created_at)
VALUES
  (1, 1, 'Add user authentication feature', 'Implement JWT-based authentication', 'john_dev', DATEADD('HOUR', -2, CURRENT_TIMESTAMP)),
  (1, 2, 'Fix security vulnerability in login', 'Patch SQL injection issue', 'jane_dev', DATEADD('HOUR', -1, CURRENT_TIMESTAMP)),
  (2, 1, 'Implement REST API endpoints', 'Add CRUD operations for users', 'bob_dev', DATEADD('HOUR', -3, CURRENT_TIMESTAMP)),
  (2, 2, 'Add input validation', 'Validate all user inputs', 'alice_dev', DATEADD('MINUTE', -30, CURRENT_TIMESTAMP)),
  (3, 1, 'Update UI components', 'Modernize the dashboard', 'charlie_dev', DATEADD('HOUR', -4, CURRENT_TIMESTAMP));

-- 샘플 Review 데이터
INSERT INTO reviews (pull_request_id, commit_sha, review_status, total_comments, severity_counts, ai_model, tokens_used, processing_time_ms, created_at)
VALUES
  (1, 'abc123def456', 'completed', 5, '{"info": 2, "warning": 2, "error": 1}', 'gpt-4', 1500, 4500, DATEADD('HOUR', -2, CURRENT_TIMESTAMP)),
  (2, 'def456ghi789', 'completed', 3, '{"info": 1, "warning": 1, "error": 1}', 'gpt-4', 1200, 3800, DATEADD('HOUR', -1, CURRENT_TIMESTAMP)),
  (3, 'ghi789jkl012', 'completed', 7, '{"info": 4, "warning": 2, "error": 1}', 'gpt-4', 2000, 5200, DATEADD('HOUR', -3, CURRENT_TIMESTAMP)),
  (4, 'jkl012mno345', 'completed', 2, '{"info": 1, "warning": 1, "error": 0}', 'gpt-4', 800, 2500, DATEADD('MINUTE', -30, CURRENT_TIMESTAMP)),
  (5, 'mno345pqr678', 'in_progress', 0, '{}', 'gpt-4', 0, 0, DATEADD('HOUR', -4, CURRENT_TIMESTAMP));

-- 샘플 Comment 데이터
INSERT INTO comments (review_id, file_path, line_number, severity, category, message, suggestion, created_at)
VALUES
  -- Review 1 comments
  (1, 'src/main/java/com/example/auth/AuthService.java', 45, 'error', 'security', '비밀번호가 평문으로 저장되고 있습니다.', '비밀번호를 BCrypt로 해싱하여 저장하세요.', DATEADD('HOUR', -2, CURRENT_TIMESTAMP)),
  (1, 'src/main/java/com/example/auth/AuthService.java', 67, 'warning', 'performance', '매번 DB 조회를 수행하고 있습니다.', '캐싱을 활용하여 성능을 개선하세요.', DATEADD('HOUR', -2, CURRENT_TIMESTAMP)),
  (1, 'src/main/java/com/example/auth/JwtUtil.java', 23, 'warning', 'security', 'JWT 시크릿 키가 하드코딩되어 있습니다.', '환경 변수로 관리하세요.', DATEADD('HOUR', -2, CURRENT_TIMESTAMP)),
  (1, 'src/main/java/com/example/model/User.java', 12, 'info', 'style', 'Lombok @Data 사용을 권장합니다.', '@Getter, @Setter 대신 @Data를 사용하세요.', DATEADD('HOUR', -2, CURRENT_TIMESTAMP)),
  (1, 'src/test/java/com/example/auth/AuthServiceTest.java', 34, 'info', 'testing', '엣지 케이스 테스트가 부족합니다.', 'null 입력에 대한 테스트를 추가하세요.', DATEADD('HOUR', -2, CURRENT_TIMESTAMP)),

  -- Review 2 comments
  (2, 'src/main/java/com/example/user/UserController.java', 89, 'error', 'security', 'SQL Injection 취약점이 있습니다.', 'PreparedStatement를 사용하세요.', DATEADD('HOUR', -1, CURRENT_TIMESTAMP)),
  (2, 'src/main/java/com/example/user/UserController.java', 102, 'warning', 'validation', '입력 검증이 누락되었습니다.', '@Valid 어노테이션을 추가하세요.', DATEADD('HOUR', -1, CURRENT_TIMESTAMP)),
  (2, 'src/main/java/com/example/config/SecurityConfig.java', 45, 'info', 'security', 'CORS 설정을 검토하세요.', '프로덕션에서는 특정 도메인만 허용하세요.', DATEADD('HOUR', -1, CURRENT_TIMESTAMP)),

  -- Review 3 comments
  (3, 'src/main/java/com/example/api/UserApi.java', 56, 'error', 'bug', 'NullPointerException 가능성이 있습니다.', 'Optional을 사용하거나 null 체크를 추가하세요.', DATEADD('HOUR', -3, CURRENT_TIMESTAMP)),
  (3, 'src/main/java/com/example/api/UserApi.java', 78, 'warning', 'performance', 'N+1 쿼리 문제가 발생할 수 있습니다.', 'Fetch Join을 사용하세요.', DATEADD('HOUR', -3, CURRENT_TIMESTAMP)),
  (3, 'src/main/java/com/example/api/UserApi.java', 90, 'warning', 'style', '메서드가 너무 깁니다 (50줄 초과).', '여러 메서드로 분리하세요.', DATEADD('HOUR', -3, CURRENT_TIMESTAMP)),
  (3, 'src/main/java/com/example/dto/UserDto.java', 12, 'info', 'documentation', 'JavaDoc이 누락되었습니다.', '클래스와 메서드에 문서를 추가하세요.', DATEADD('HOUR', -3, CURRENT_TIMESTAMP)),
  (3, 'src/main/java/com/example/service/UserService.java', 123, 'info', 'refactoring', '중복된 코드가 있습니다.', '공통 메서드로 추출하세요.', DATEADD('HOUR', -3, CURRENT_TIMESTAMP)),
  (3, 'src/main/java/com/example/service/UserService.java', 145, 'info', 'testing', '단위 테스트가 필요합니다.', '테스트 케이스를 작성하세요.', DATEADD('HOUR', -3, CURRENT_TIMESTAMP)),
  (3, 'src/main/resources/application.yml', 23, 'info', 'configuration', '하드코딩된 설정이 있습니다.', '프로파일별로 분리하세요.', DATEADD('HOUR', -3, CURRENT_TIMESTAMP)),

  -- Review 4 comments
  (4, 'src/main/java/com/example/validation/InputValidator.java', 34, 'warning', 'validation', '정규식이 복잡합니다.', '가독성을 위해 주석을 추가하세요.', DATEADD('MINUTE', -30, CURRENT_TIMESTAMP)),
  (4, 'src/test/java/com/example/validation/InputValidatorTest.java', 45, 'info', 'testing', '테스트 케이스가 잘 작성되었습니다.', '', DATEADD('MINUTE', -30, CURRENT_TIMESTAMP));

-- 샘플 Review Rules 데이터
INSERT INTO review_rules (repository_id, name, description, rule_type, rule_config, enabled, priority, target_files, created_at, updated_at)
VALUES (NULL, '보안: 하드코딩된 비밀번호 검사', '코드에 하드코딩된 비밀번호나 API 키가 있는지 검사합니다.', 'code_pattern',
   '{"patterns": ["password=", "apikey="], "severity": "error"}',
   true, 100, '**/*', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO review_rules (repository_id, name, description, rule_type, rule_config, enabled, priority, target_files, created_at, updated_at)
VALUES (NULL, '성능: N+1 쿼리 감지', 'JPA에서 N+1 쿼리 문제를 검사합니다.', 'custom_prompt',
   '{"prompt": "Check for N+1 query problems", "severity": "warning"}',
   true, 80, '**/*Repository.java', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO review_rules (repository_id, name, description, rule_type, rule_config, enabled, priority, target_files, created_at, updated_at)
VALUES (1, '프로젝트별: 로깅 규칙', 'System.out.println 대신 로거를 사용하는지 검사합니다.', 'code_pattern',
   '{"patterns": ["System.out.println", "System.err.println"], "severity": "warning"}',
   true, 70, '**/*.java', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
