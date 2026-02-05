package com.eventhub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EventHub API")
                        .version("1.0.0")
                        .description("""
                                EventHub - Event Management and Ticketing Platform
                                
                                Features:
                                - Event creation and management (Admin only)
                                - Ticket purchasing and management
                                - AI-powered event recommendations
                                - User authentication with JWT
                                
                                Authentication:
                                1. Register: POST /api/auth/register
                                2. Login: POST /api/auth/login → Get JWT token
                                3. Use token: Click "Authorize" button, enter "Bearer <token>"
                                4. Test authenticated endpoints
                                """)
                        .contact(new Contact()
                                .name("EventHub Team")
                                .email("support@eventhub.com")
                                .url("https://eventhub.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))

                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token (obtained from /api/auth/login)")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"));
    }
}