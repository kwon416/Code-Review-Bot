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
     * 언어별 특화 가이드라인을 포함한 프롬프트를 생성합니다
     */
    public String buildCodeReviewPrompt(String diffContent, String language) {
        String languageSpecific = LANGUAGE_SPECIFIC_GUIDELINES.getOrDefault(
                language,
                "General code review guidelines apply."
        );

        return """
            You are an expert code reviewer specializing in %s. Analyze the following code diff and provide detailed feedback.

            Focus on:
            1. Bugs and potential errors
            2. Performance issues
            3. Security vulnerabilities
            4. Code style and best practices
            5. Maintainability concerns

            %s

            Code Diff:
            ```
            %s
            ```

            Provide your review in the following JSON format:
            {
              "summary": "Overall summary of the code review (2-3 sentences)",
              "comments": [
                {
                  "filePath": "path/to/file",
                  "lineNumber": 10,
                  "severity": "warning",
                  "category": "performance",
                  "message": "Brief description of the issue",
                  "suggestion": "How to fix or improve",
                  "codeExample": "Example of improved code (optional)"
                }
              ]
            }

            Severity levels: info, warning, error
            Categories: bug, performance, security, style, best-practice, maintainability

            Only include meaningful comments. Skip trivial formatting issues.
            Prioritize security and correctness over style.
            Be specific and actionable in your suggestions.
            """.formatted(language, languageSpecific, diffContent);
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
