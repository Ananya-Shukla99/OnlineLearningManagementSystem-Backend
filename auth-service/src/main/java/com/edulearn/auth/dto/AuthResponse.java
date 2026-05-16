package com.edulearn.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private boolean success;
    private String message;
    private String token;
    private Long userId;
    private String email;
    private String fullName;
    private String role;
    private String bio;
    private String mobile;
    private String headline;
    private String expertise;
}
