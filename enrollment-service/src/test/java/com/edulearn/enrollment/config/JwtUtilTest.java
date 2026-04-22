package com.edulearn.enrollment.config;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "jwt.secret=mySecretKeyForJWTAuthenticationInEduLearnMicroserviceArchitecture2024SuperSecureKey123!@#",
    "jwt.expiration=86400000"
})
@DisplayName("JWT Utility Tests")
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    private String validToken;
    private final String SECRET_KEY = "mySecretKeyForJWTAuthenticationInEduLearnMicroserviceArchitecture2024SuperSecureKey123!@#";

    @BeforeEach
    void setUp() {
        // Create a valid token
        validToken = Jwts.builder()
                .setSubject("test@example.com")
                .claim("role", "STUDENT")
                .claim("userId", 10)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    @Test
    @DisplayName("Should extract email from valid token")
    void testExtractEmail() {
        // Act
        String email = jwtUtil.extractEmail(validToken);

        // Assert
        assertEquals("test@example.com", email);
    }

    @Test
    @DisplayName("Should extract role from valid token")
    void testExtractRole() {
        // Act
        String role = jwtUtil.extractRole(validToken);

        // Assert
        assertEquals("STUDENT", role);
    }

    @Test
    @DisplayName("Should extract userId from valid token")
    void testExtractUserId() {
        // Act
        Integer userId = jwtUtil.extractUserId(validToken);

        // Assert
        assertEquals(10, userId);
    }

    @Test
    @DisplayName("Should extract all claims from valid token")
    void testExtractAllClaims() {
        // Act
        Claims claims = jwtUtil.extractAllClaims(validToken);

        // Assert
        assertNotNull(claims);
        assertEquals("test@example.com", claims.getSubject());
        assertEquals("STUDENT", claims.get("role"));
        assertEquals(10, claims.get("userId"));
    }

    @Test
    @DisplayName("Should throw exception for invalid token signature")
    void testInvalidTokenSignature() {
        // Arrange
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwicm9sZSI6IlNUVURFTlQiLCJ1c2VySWQiOjEwfQ.invalidSignature";

        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtUtil.extractAllClaims(invalidToken);
        });
    }

    @Test
    @DisplayName("Should throw exception for malformed token")
    void testMalformedToken() {
        // Arrange
        String malformedToken = "notAValidToken";

        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtUtil.extractAllClaims(malformedToken);
        });
    }

    @Test
    @DisplayName("Should handle token with different role")
    void testTokenWithDifferentRole() {
        // Arrange
        String instructorToken = Jwts.builder()
                .setSubject("instructor@example.com")
                .claim("role", "INSTRUCTOR")
                .claim("userId", 20)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();

        // Act
        String role = jwtUtil.extractRole(instructorToken);
        Integer userId = jwtUtil.extractUserId(instructorToken);

        // Assert
        assertEquals("INSTRUCTOR", role);
        assertEquals(20, userId);
    }
}

