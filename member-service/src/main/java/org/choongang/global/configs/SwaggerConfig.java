package org.choongang.global.configs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(info=@Info(title="회원인증 API", description = "회원 가입 및 인증 토큰 발급"))
@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi apiGroup() {
        return GroupedOpenApi.builder()
                .group("회원인증 API v1")
                .pathsToMatch("/**")
                .build();
    }
}