package com.edulearn.lesson.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI lessonOpenAPI() {
        // Use relative server URL so Swagger Try-It-Out works
        // behind gateway and in direct local service access.
        return new OpenAPI().servers(List.of(new Server().url("/")));
    }
}
