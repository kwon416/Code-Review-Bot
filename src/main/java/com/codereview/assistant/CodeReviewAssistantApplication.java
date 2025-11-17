package com.codereview.assistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CodeReviewAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeReviewAssistantApplication.class, args);
    }
}
