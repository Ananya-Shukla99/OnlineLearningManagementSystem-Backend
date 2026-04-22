package com.edulearn.apigateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeExchange(exchange -> exchange
                // Allow CORS preflight requests
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Allow Swagger/OpenAPI documentation
                .pathMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs",
                    "/v3/api-docs/**",
                    "/swagger-resources",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                // Allow public auth endpoints (registration, login, token validation)
                .pathMatchers("/auth/register").permitAll()
                .pathMatchers("/auth/login").permitAll()
                .pathMatchers("/auth/validate").permitAll()
                .pathMatchers("/auth/refresh").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/v1/courses/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/v1/lessons/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/v1/progress/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/v1/discussion/**").permitAll()
                // All other requests pass through to backend services
                .anyExchange().permitAll()
            );
        return http.build();
    }
}
