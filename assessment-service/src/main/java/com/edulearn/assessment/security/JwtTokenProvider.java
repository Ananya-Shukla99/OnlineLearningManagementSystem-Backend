package com.edulearn.assessment.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

	@Value("${jwt.secret:mySecretKeyForJWTAuthenticationInEduLearnMicroserviceArchitecture2024SuperSecureKey123!@#}")
	private String jwtSecret;

	@Value("${jwt.expiration:86400000}")
	private long jwtExpirationMs;

	/**
	 * Generate JWT Token
	 */
	public String generateToken(String userId, String email) {
		SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

		return Jwts.builder().setSubject(userId).claim("email", email).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
				.signWith(key, SignatureAlgorithm.HS512).compact();
	}

	/**
	 * Validate JWT Token
	 */
	public boolean validateToken(String token) {
		try {
			SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
			Jwts.parser().verifyWith(key).build().parseClaimsJws(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Get User ID from Token
	 */
	public String getUserIdFromToken(String token) {
		try {
			SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
			Claims claims = Jwts.parser().verifyWith(key).build().parseClaimsJws(token).getBody();
			return claims.getSubject();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Get Email from Token
	 */
	public String getEmailFromToken(String token) {
		try {
			SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
			Claims claims = Jwts.parser().verifyWith(key).build().parseClaimsJws(token).getBody();
			return claims.get("email", String.class);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Get Expiration Time from Token
	 */
	public Date getExpirationDateFromToken(String token) {
		try {
			SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
			Claims claims = Jwts.parser().verifyWith(key).build().parseClaimsJws(token).getBody();
			return claims.getExpiration();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Check if Token is Expired
	 */
	public boolean isTokenExpired(String token) {
		Date expiration = getExpirationDateFromToken(token);
		return expiration != null && expiration.before(new Date());
	}

	/**
	 * Extract Bearer Token from Authorization Header
	 */
	public String extractTokenFromHeader(String authHeader) {
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			return authHeader.substring(7);
		}
		return null;
	}
}
