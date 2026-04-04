package com.edulearn.lesson.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JWT Utility Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String SECRET_KEY = "mySecretKeyForJWTAuthenticationInEduLearnMicroserviceArchitecture2024SuperSecureKey123!@#";
    private String validToken;
    private String invalidToken;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        // Create a valid token
        validToken = Jwts.builder()
                .setSubject("test@example.com")
                .claim("role", "STUDENT")
                .claim("userId", 1)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();

        invalidToken = "invalid.token.here";
    }

    @Test
    @DisplayName("Should extract email from valid token")
    void testExtractEmail() {
        // Act
        String token = Jwts.builder()
                .setSubject("john@example.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();

        String email = jwtUtil.extractEmail(token);

        // Assert
        assertEquals("john@example.com", email);
    }

    @Test
    @DisplayName("Should extract role from valid token")
    void testExtractRole() {
        // Act
        String token = Jwts.builder()
                .setSubject("test@example.com")
                .claim("role", "INSTRUCTOR")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();

        String role = jwtUtil.extractRole(token);

        // Assert
        assertEquals("INSTRUCTOR", role);
    }

    @Test
    @DisplayName("Should extract userId from valid token")
    void testExtractUserId() {
        // Act
        String token = Jwts.builder()
                .setSubject("test@example.com")
                .claim("userId", 42)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();

        Integer userId = jwtUtil.extractUserId(token);

        // Assert
        assertEquals(42, userId);
    }

    @Test
    @DisplayName("Should validate correct token as true")
    void testIsTokenValidTrue() {
        // Arrange
        String token = Jwts.builder()
                .setSubject("test@example.com")
                .claim("role", "STUDENT")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();

        // Act
        boolean isValid = jwtUtil.isTokenValid(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should validate malformed token as false")
    void testIsTokenValidMalformed() {
        // Act
        boolean isValid = jwtUtil.isTokenValid("malformed.token");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should validate expired token as false")
    void testIsTokenValidExpired() {
        // Arrange - Create token that expired 1 second ago
        String expiredToken = Jwts.builder()
                .setSubject("test@example.com")
                .claim("role", "STUDENT")
                .setIssuedAt(new Date(System.currentTimeMillis() - 10000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();

        // Act
        boolean isValid = jwtUtil.isTokenValid(expiredToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should extract claims from valid token")
    void testExtractClaims() {
        // Arrange
        String token = Jwts.builder()
                .setSubject("test@example.com")
                .claim("role", "ADMIN")
                .claim("userId", 5)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();

        // Act
        Claims claims = jwtUtil.extractClaims(token);

        // Assert
        assertNotNull(claims);
        assertEquals("test@example.com", claims.getSubject());
        assertEquals("ADMIN", claims.get("role"));
        assertEquals(5, claims.get("userId"));
    }

    @Test
    @DisplayName("Should throw exception when extracting claims from invalid token")
    void testExtractClaimsInvalidToken() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtUtil.extractClaims("invalid.token.data");
        });
    }

    @Test
    @DisplayName("Should handle token with all required fields")
    void testTokenWithAllFields() {
        // Arrange
        String token = Jwts.builder()
                .setSubject("student@example.com")
                .claim("role", "STUDENT")
                .claim("userId", 10)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();

        // Act
        assertTrue(jwtUtil.isTokenValid(token));
        assertEquals("student@example.com", jwtUtil.extractEmail(token));
        assertEquals("STUDENT", jwtUtil.extractRole(token));
        assertEquals(10, jwtUtil.extractUserId(token));
    }

    @Test
    @DisplayName("Should differentiate between different roles in tokens")
    void testDifferentRoles() {
        // Arrange
        String studentToken = Jwts.builder()
                .setSubject("student@example.com")
                .claim("role", "STUDENT")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();

        String instructorToken = Jwts.builder()
                .setSubject("instructor@example.com")
                .claim("role", "INSTRUCTOR")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();

        // Act & Assert
        assertEquals("STUDENT", jwtUtil.extractRole(studentToken));
        assertEquals("INSTRUCTOR", jwtUtil.extractRole(instructorToken));
        assertNotEquals(jwtUtil.extractRole(studentToken), jwtUtil.extractRole(instructorToken));
    }
}

