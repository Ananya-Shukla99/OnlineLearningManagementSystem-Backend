package com.edulearn.assessment.security;

/**
 * Security Constants
 */
public class SecurityConstants {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String TOKEN_TYPE = "JWT";

    // Token claims
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_ROLES = "roles";

    // Security endpoints
    public static final String[] PUBLIC_ENDPOINTS = {
        "/api/v1/auth/**",
        "/api/v1/health/**",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/swagger-ui.html"
    };

    // Error messages
    public static final String INVALID_TOKEN = "Invalid JWT token";
    public static final String EXPIRED_TOKEN = "JWT token has expired";
    public static final String UNSUPPORTED_TOKEN = "JWT token is unsupported";
    public static final String EMPTY_TOKEN = "JWT claims string is empty";
    public static final String NO_BEARER_TOKEN = "Bearer token not found in Authorization header";

    private SecurityConstants() {
        // Private constructor to prevent instantiation
    }
}

