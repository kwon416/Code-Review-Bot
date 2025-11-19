package com.codereview.assistant.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 언어별 특화 프롬프트 제공 서비스
 */
@Service
public class LanguageSpecificPromptService {

    private static final Map<String, String> LANGUAGE_SPECIFIC_GUIDELINES = new HashMap<>();

    static {
        // Java 특화 가이드라인
        LANGUAGE_SPECIFIC_GUIDELINES.put("Java", """
            Java-specific review guidelines:
            - Check for proper use of Java 17+ features (records, sealed classes, pattern matching)
            - Verify exception handling (try-with-resources, proper exception hierarchy)
            - Look for potential NullPointerExceptions (use Optional where appropriate)
            - Check for thread safety issues (synchronization, volatile, AtomicInteger)
            - Verify proper use of generics and type safety
            - Check for memory leaks (unclosed streams, listeners, large collections)
            - Look for improper equals/hashCode implementations
            - Verify proper use of Java collections and streams
            - Check for dependency injection best practices (Spring)
            - Look for SQL injection vulnerabilities in database queries
            """);

        // Python 특화 가이드라인
        LANGUAGE_SPECIFIC_GUIDELINES.put("Python", """
            Python-specific review guidelines:
            - Check for PEP 8 style compliance
            - Verify proper use of type hints (Python 3.5+)
            - Look for mutable default arguments
            - Check for proper exception handling (specific exceptions, not bare except)
            - Verify proper use of context managers (with statements)
            - Look for inefficient list comprehensions or loops
            - Check for proper use of generators for large datasets
            - Verify proper use of async/await for I/O operations
            - Look for security issues (eval, exec, pickle, yaml.load)
            - Check for proper virtual environment and dependency management
            """);

        // JavaScript 특화 가이드라인
        LANGUAGE_SPECIFIC_GUIDELINES.put("JavaScript", """
            JavaScript-specific review guidelines:
            - Check for proper use of const/let instead of var
            - Verify async/await usage and promise handling
            - Look for potential XSS vulnerabilities
            - Check for proper error handling in async code
            - Verify proper use of arrow functions and binding
            - Look for memory leaks (event listeners, closures)
            - Check for prototype pollution vulnerabilities
            - Verify proper use of Array methods (map, filter, reduce)
            - Look for improper use of == instead of ===
            - Check for proper handling of null/undefined
            """);

        // TypeScript 특화 가이드라인
        LANGUAGE_SPECIFIC_GUIDELINES.put("TypeScript", """
            TypeScript-specific review guidelines:
            - Verify proper type annotations and avoid 'any' type
            - Check for proper use of interfaces vs types
            - Look for missing null/undefined checks (strict null checks)
            - Verify proper use of generics
            - Check for proper use of enums vs union types
            - Look for type assertions that might hide errors
            - Verify proper use of readonly and const assertions
            - Check for proper handling of Promise types
            - Look for missing return types on functions
            - Verify proper use of utility types (Partial, Required, Pick, etc.)
            """);

        // Go 특화 가이드라인
        LANGUAGE_SPECIFIC_GUIDELINES.put("Go", """
            Go-specific review guidelines:
            - Check for proper error handling (never ignore errors)
            - Verify proper use of defer for resource cleanup
            - Look for goroutine leaks and race conditions
            - Check for proper use of channels and select statements
            - Verify proper use of context for cancellation
            - Look for mutex deadlocks
            - Check for proper use of interfaces (small, focused)
            - Verify proper use of pointers vs values
            - Look for inefficient string concatenation
            - Check for proper slice/map initialization and capacity
            """);

        // Rust 특화 가이드라인
        LANGUAGE_SPECIFIC_GUIDELINES.put("Rust", """
            Rust-specific review guidelines:
            - Check for unnecessary use of unsafe blocks
            - Verify proper lifetime annotations
            - Look for potential panics (unwrap, expect usage)
            - Check for proper use of Result and Option types
            - Verify proper ownership and borrowing
            - Look for inefficient clone operations
            - Check for proper use of traits and generics
            - Verify proper error propagation with ? operator
            - Look for missing error handling
            - Check for proper use of iterators vs loops
            """);

        // C++ 특화 가이드라인
        LANGUAGE_SPECIFIC_GUIDELINES.put("C++", """
            C++-specific review guidelines:
            - Check for memory leaks and use-after-free
            - Verify proper use of smart pointers (unique_ptr, shared_ptr)
            - Look for buffer overflows and array bounds violations
            - Check for proper RAII (Resource Acquisition Is Initialization)
            - Verify proper move semantics and copy constructors
            - Look for missing virtual destructors in base classes
            - Check for proper const correctness
            - Verify proper exception safety guarantees
            - Look for undefined behavior
            - Check for proper use of C++17/20/23 features
            """);
    }

    /**
     * 언어별 특화 가이드라인을 포함한 프롬프트를 생성합니다 (토큰 최적화)
     */
    public String buildCodeReviewPrompt(String diffContent, String language) {
        String focus = getFocusAreas(language);

        return """
            당신은 %s 코드 리뷰 전문가입니다.

            **응답 언어: 한국어만 사용**

            ## 검토 중점 사항
            %s

            ## 변경 내역 (Diff)
            ```diff
            %s
            ```

            ## 리뷰 원칙 (중요!)
            1. **실제로 문제가 되는 것만 지적**
               - 버그: 런타임 에러, 로직 오류, 예외 처리 누락
               - 보안: SQL 인젝션, XSS, 인증/권한 문제, 민감정보 노출
               - 성능: 명백한 성능 저하 (N+1 쿼리, 무한루프, 메모리 누수)

            2. **지적하지 말아야 할 것**
               - 변수/함수 명명 규칙
               - 코드 스타일 (들여쓰기, 줄바꿈 등)
               - 주석 추가 제안
               - 사소한 리팩토링 제안
               - 개인 선호도에 따른 의견

            3. **응답 규칙**
               - 이슈가 없으면 빈 배열 반환 (억지로 찾지 말 것)
               - 관련된 여러 문제는 하나로 통합
               - 최대 3개까지만 (우선순위 높은 것만)
               - 구체적인 해결 방법 제시

            ## 응답 형식 (JSON)
            ```json
            {
              "summary": "전체 요약 또는 '이슈 없음'",
              "comments": [
                {
                  "filePath": "파일 경로",
                  "lineNumber": 줄 번호,
                  "severity": "error|warning|info",
                  "category": "bug|security|performance",
                  "message": "문제 설명 (무엇이 문제인지)",
                  "suggestion": "해결 방법 (어떻게 고칠지)"
                }
              ]
            }
            ```

            ## 좋은 예시
            ✅ "SQL 쿼리에 사용자 입력이 직접 포함되어 SQL 인젝션 위험이 있습니다. PreparedStatement를 사용하세요."
            ✅ "null 체크 없이 메서드를 호출하여 NullPointerException이 발생할 수 있습니다."

            ## 나쁜 예시
            ❌ "변수명을 더 명확하게 변경하세요."
            ❌ "주석을 추가하면 좋겠습니다."
            ❌ "코드를 리팩토링하면 더 깔끔해질 것 같습니다."

            **리뷰를 시작하세요. 실제 문제만 찾으세요.**
            """.formatted(language, focus, diffContent);
    }

    /**
     * Get concise focus areas for a language
     */
    private String getFocusAreas(String language) {
        return switch (language) {
            case "Java" ->
                "- SQL 인젝션, XSS 취약점\n" +
                "- NullPointerException 위험\n" +
                "- 리소스 미해제 (스트림, 커넥션)\n" +
                "- 동시성 문제 (Race condition)";

            case "Python" ->
                "- 보안 위험 (eval, exec, pickle)\n" +
                "- 타입 오류 및 예외 처리\n" +
                "- 메모리 누수 (순환 참조)\n" +
                "- 비동기 처리 오류";

            case "JavaScript", "TypeScript" ->
                "- XSS, 인젝션 공격\n" +
                "- Promise/async 처리 오류\n" +
                "- 메모리 누수 (이벤트 리스너)\n" +
                "- null/undefined 처리";

            case "Go" ->
                "- 에러 처리 누락\n" +
                "- Goroutine 누수\n" +
                "- Race condition\n" +
                "- nil pointer 참조";

            case "Rust" ->
                "- unsafe 블록 남용\n" +
                "- panic 발생 가능성\n" +
                "- 생명주기 문제\n" +
                "- 소유권 규칙 위반";

            case "C++" ->
                "- 메모리 누수, Use-after-free\n" +
                "- 버퍼 오버플로우\n" +
                "- 미정의 동작\n" +
                "- RAII 미준수";

            default ->
                "- 보안 취약점\n" +
                "- 치명적 버그\n" +
                "- 성능 저하\n" +
                "- 예외 처리 누락";
        };
    }

    /**
     * 지원하는 언어 목록을 반환합니다
     */
    public boolean isLanguageSupported(String language) {
        return LANGUAGE_SPECIFIC_GUIDELINES.containsKey(language);
    }

    /**
     * 지원하는 모든 언어 목록을 반환합니다
     */
    public String[] getSupportedLanguages() {
        return LANGUAGE_SPECIFIC_GUIDELINES.keySet().toArray(new String[0]);
    }
}
