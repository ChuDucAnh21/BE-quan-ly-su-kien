package com.example.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        final String cookieAuthName = "cookieAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Event Management API")
                        .version("1.0")
                        .description("Tài liệu API cho Hệ thống Quản lý Sự kiện và Check-in QR."))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName).addList(cookieAuthName))
                .components(new Components()
                        // 1. Cấu hình bảo mật qua Bearer Token (Nếu FE dùng Header)
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                        // 2. Cấu hình bảo mật qua Cookie (Bởi vì hệ thống chúng ta đang xài Cookie)
                        .addSecuritySchemes(cookieAuthName, new SecurityScheme()
                                .name("access_token")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)));
    }
}
