package com.edulearn.assessment.exception;

/**
 * Exception thrown when JWT token has expired
 */
public class JwtTokenExpiredException extends RuntimeException {

    public JwtTokenExpiredException(String message) {
        super(message);
    }

    public JwtTokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}

