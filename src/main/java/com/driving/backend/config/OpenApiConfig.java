package com.driving.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI backendOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Taxi Driver Backend API")
                        .description("OSM 기반 경로 탐색, 유저 프로필, 설문 API 문서입니다.")
                        .version("v1")
                        .contact(new Contact().name("Taxi Driver BE Team")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Authorization 헤더에 `Bearer {token}` 형식으로 전달합니다.")));
    }
}
