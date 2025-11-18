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
            %s 코드 리뷰. 중점: %s

            **한국어로만 응답하세요.**

            Diff:
            ```
            %s
            ```

            JSON 형식:
            {"summary":"요약","comments":[{"filePath":"","lineNumber":0,"severity":"error|warning|info","category":"bug|security|performance","message":"설명","suggestion":"제안"}]}

            규칙: 최대 5개 주요 이슈만. 사소한 스타일 제외.
            """.formatted(language, focus, diffContent);
    }

    /**
     * Get concise focus areas for a language
     */
    private String getFocusAreas(String language) {
        return switch (language) {
            case "Java" -> "Security (SQL injection, XSS), NullPointers, Resource leaks, Thread safety";
            case "Python" -> "Security (eval, exec), Type errors, Exceptions, Memory issues";
            case "JavaScript", "TypeScript" -> "Security (XSS, injection), Async errors, Type safety, Memory leaks";
            case "Go" -> "Error handling, Goroutine leaks, Race conditions, nil pointers";
            case "Rust" -> "Unsafe code, Panics, Lifetime issues, Ownership bugs";
            case "C++" -> "Memory leaks, Buffer overflows, Use-after-free, Undefined behavior";
            default -> "Security vulnerabilities, Critical bugs, Performance issues";
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
