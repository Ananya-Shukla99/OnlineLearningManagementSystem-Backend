package com.edulearn.progress.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Certificate entity for course completion certificates
 * Stores generated certificate details and verification code
 */
@Entity
@Table(name = "certificates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer certificateId;

    @Column(nullable = false)
    private Integer studentId;

    @Column(nullable = false)
    private Integer courseId;

    @Column(name = "issued_at", nullable = false)
    private LocalDate issuedAt;

    @Column(name = "certificate_url")
    private String certificateUrl;

    @Column(name = "verification_code", unique = true)
    private String verificationCode;

    @Column(name = "instructor_name")
    private String instructorName;

    @Column(name = "course_name")
    private String courseName;

    @PrePersist
    protected void onCreate() {
        if (issuedAt == null) {
           issuedAt = LocalDate.now();
        }
    }


}

