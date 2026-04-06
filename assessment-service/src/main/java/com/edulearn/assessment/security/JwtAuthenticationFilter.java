package com.edulearn.assessment.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * JWT Authentication Filter
 * Intercepts every request and validates JWT token from Authorization header
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && !authHeader.isEmpty()) {
                // Extract token from Bearer header
                String token = jwtTokenProvider.extractTokenFromHeader(authHeader);

                if (token != null && jwtTokenProvider.validateToken(token)) {
                    // Token is valid
                    String userId = jwtTokenProvider.getUserIdFromToken(token);
                    String email = jwtTokenProvider.getEmailFromToken(token);

                    if (userId != null && !userId.isEmpty()) {
                        // Create authentication object
                        UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, email, new ArrayList<>());

                        // Set authentication in security context
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}");
        }

        filterChain.doFilter(request, response);
    }
}

