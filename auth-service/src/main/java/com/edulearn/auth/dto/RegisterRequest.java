package com.edulearn.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String email;
    private String fullName;
    private String password;
    private String role; // "STUDENT", "INSTRUCTOR", "ADMIN"
}
