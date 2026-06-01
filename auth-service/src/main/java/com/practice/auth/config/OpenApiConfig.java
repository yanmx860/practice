package com.practice.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** * OpenAPI/Swagger 文档配置 * @author ymx * @since 2026-03-15 */
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI practiceOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("认证服务 API")
                        .description("用户认证、授权、令牌管理")
                        .version("1.0.0")
                        .contact(new Contact().name("ymx")));
    }
}
