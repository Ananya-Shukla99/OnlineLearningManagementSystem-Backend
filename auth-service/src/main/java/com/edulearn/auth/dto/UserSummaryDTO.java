package com.edulearn.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDTO {
    private Long userId;
    private String email;
    private String fullName;
    private String role;
    private String bio;
    private String mobile;
    private LocalDateTime createdAt;
    }
