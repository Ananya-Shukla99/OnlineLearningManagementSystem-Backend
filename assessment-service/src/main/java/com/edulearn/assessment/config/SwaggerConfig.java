package com.edulearn.assessment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI Configuration for Assessment Service
 *
 * Swagger UI will be available at: http://localhost:8084/swagger-ui.html
 * OpenAPI JSON will be available at: http://localhost:8084/v3/api-docs
 *
 * Base URL for APIs: http://localhost:8084/api/v1/assessments
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8084")
                                .description("Local Development Server - Assessment Service"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("API Gateway - Through Gateway")
                ))
                .info(new Info()
                        .title("Assessment Service API")
                        .version("1.0.0")
                        .description("REST API for Quiz and Assessment Management in EduLearn LMS\n\n" +
                                "**Base Path**: /api/v1/assessments\n\n" +
                                "**Features**:\n" +
                                "- Quiz management (CRUD operations)\n" +
                                "- Question management with multiple question types (MCQ_SINGLE, TRUE_FALSE, MCQ_MULTI)\n" +
                                "- Student attempt tracking\n" +
                                "- Automatic answer grading with intelligent scoring\n" +
                                "- Score calculation and performance reporting\n" +
                                "- Redis-based quiz timer management\n\n" +
                                "**Question Types**:\n" +
                                "1. MCQ_SINGLE: Single choice multiple choice (exact match)\n" +
                                "2. TRUE_FALSE: Boolean questions\n" +
                                "3. MCQ_MULTI: Multiple choice multiple select (all correct answers required)\n\n" +
                                "**Auto-Grading**: Questions are automatically graded based on correct answers. " +
                                "Final score is calculated as (marks earned / total marks) * 100")
                        .contact(new Contact()
                                .name("EduLearn Development Team")
                                .email("dev@edulearn.com")
                                .url("https://edulearn.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html"))
                );
    }
}

