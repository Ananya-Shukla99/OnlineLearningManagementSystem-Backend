package com.edulearn.apigateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Centralized JWT authentication filter for the API Gateway.
 *
 * <p>Validates every non-public request's Bearer token, extracts user claims
 * (userId, role, email), and forwards them as headers to downstream services.
 *
 * <p>Role-based authorization can be enforced here per-route once all services
 * are stable — see TODO markers below.
 */
@Component
public class JwtGatewayFilter implements WebFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtGatewayFilter.class);

    private final SecretKey signingKey;

    public JwtGatewayFilter(@Value("${jwt.secret}") String jwtSecret) {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        HttpMethod method = exchange.getRequest().getMethod();

        // ── Public endpoints — no token required ──────────────────────────────
        if (isPublic(exchange)) {
            log.debug("GATEWAY AUTH: public route — {} {}", method, path);
            // Even on public routes, if a token is present, extract and forward claims
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    Claims claims = parseToken(authHeader.substring(7));
                    ServerHttpRequest mutated = enrichRequestWithClaims(exchange.getRequest(), claims);
                    return chain.filter(exchange.mutate().request(mutated).build());
                } catch (JwtException ex) {
                    // Token invalid on a public route — just pass through without claims
                    log.debug("GATEWAY AUTH: invalid token on public route (ignored) — {}", ex.getMessage());
                }
            }
            return chain.filter(exchange);
        }

        // ── Protected endpoints — token required ──────────────────────────────
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("GATEWAY AUTH DENIED: missing or malformed Authorization header — {} {}", method, path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = parseToken(token);
            String userId = claims.getSubject();
            String role = (String) claims.get("role");
            String email = (String) claims.get("email");

            log.debug("GATEWAY AUTH OK: userId={}, role={}, email={} — {} {}", userId, role, email, method, path);

            // ── Role-based route enforcement ──────────────────────────────────
            // TODO: re-enable strict role checks once services are stable
            // For now, we log the expected role but do NOT block. This lets the
            // downstream @PreAuthorize annotations handle authorization.
            //
            // Example future enforcement:
            // if (path.startsWith("/api/v1/courses") && "DELETE".equals(method.name()) && !"ADMIN".equals(role)) {
            //     log.warn("GATEWAY AUTHZ DENIED: DELETE /courses requires ADMIN, user has role={}", role);
            //     exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            //     return exchange.getResponse().setComplete();
            // }

            ServerHttpRequest mutated = enrichRequestWithClaims(exchange.getRequest(), claims);
            return chain.filter(exchange.mutate().request(mutated).build());

        } catch (JwtException ex) {
            log.warn("GATEWAY AUTH DENIED: invalid JWT — {} {} — {}", method, path, ex.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    /**
     * Parse and validate a JWT token, returning the claims.
     */
    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Add X-User-Id, X-User-Role, X-User-Email headers so downstream services
     * can identify the authenticated user without re-parsing the JWT.
     */
    private ServerHttpRequest enrichRequestWithClaims(ServerHttpRequest request, Claims claims) {
        ServerHttpRequest.Builder builder = request.mutate();
        builder.header("X-User-Id", claims.getSubject());
        if (claims.get("role") != null) {
            builder.header("X-User-Role", (String) claims.get("role"));
        }
        if (claims.get("email") != null) {
            builder.header("X-User-Email", (String) claims.get("email"));
        }
        return builder.build();
    }

    /**
     * Determines whether a request targets a public (unauthenticated) endpoint.
     */
    private boolean isPublic(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        HttpMethod method = exchange.getRequest().getMethod();

        // CORS preflight
        if (HttpMethod.OPTIONS.equals(method)) {
            return true;
        }

        if (path.startsWith("/auth/login")
                || path.startsWith("/auth/register")
                || path.startsWith("/auth/refresh")
                || path.startsWith("/auth/validate")
                || path.startsWith("/auth/send-otp")
                || path.startsWith("/auth/login/google")
                || path.startsWith("/oauth2/")
                || path.startsWith("/login/oauth2/")) {
            return true;
        }

        // Swagger / OpenAPI
        if (path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/webjars")) {
            return true;
        }

        // Lesson previews — public
        if (path.startsWith("/api/v1/lessons/preview")) {
            return true;
        }

        // Public GET on courses (list, search, category, featured, details)
        if (HttpMethod.GET.equals(method) && path.startsWith("/api/v1/courses")) {
            // Admin-only routes require auth even for GET
            if (path.contains("/all") || path.contains("/pending")) {
                return false;
            }
            return true;
        }

        // Public GET on lessons, progress and discussion
        if (HttpMethod.GET.equals(method) && 
            (path.startsWith("/api/v1/lessons") || 
             path.startsWith("/api/v1/progress") || 
             path.startsWith("/api/v1/discussion"))) {
            return true;
        }

        return false;
    }

    @Override
    public int getOrder() {
        return -200;
    }
}
