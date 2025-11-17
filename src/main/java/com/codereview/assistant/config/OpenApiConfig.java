package com.codereview.assistant.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI codeReviewOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("로컬 개발 서버");

        Server prodServer = new Server();
        prodServer.setUrl("https://api.codereview.example.com");
        prodServer.setDescription("프로덕션 서버");

        Contact contact = new Contact();
        contact.setName("CodeReview AI Team");
        contact.setEmail("support@codereview.example.com");

        License license = new License()
            .name("MIT License")
            .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
            .title("CodeReview AI Assistant API")
            .version("1.0.0")
            .description("""
                AI 기반 자동 코드 리뷰 시스템 API

                ## 주요 기능
                - GitHub Webhook 연동을 통한 실시간 PR 감지
                - AI 기반 코드 분석 (버그, 성능, 보안, 베스트 프랙티스)
                - 자동 GitHub 코멘트 생성
                - 리뷰 통계 및 대시보드
                - 커스텀 리뷰 규칙 관리

                ## 인증
                현재 버전은 인증이 필요하지 않습니다. (개발 환경)
                프로덕션 환경에서는 적절한 인증 메커니즘을 추가해야 합니다.
                """)
            .contact(contact)
            .license(license);

        return new OpenAPI()
            .info(info)
            .servers(List.of(localServer, prodServer));
    }
}
