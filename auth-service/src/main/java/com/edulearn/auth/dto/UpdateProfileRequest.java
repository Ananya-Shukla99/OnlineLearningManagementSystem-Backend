package com.edulearn.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    private Long userId;
    private String fullName;
    private String bio;
    private String mobile;
    private String headline;
    private String expertise;
}
