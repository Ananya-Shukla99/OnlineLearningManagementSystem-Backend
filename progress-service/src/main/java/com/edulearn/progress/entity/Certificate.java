package com.edulearn.progress.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;


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
    @Column(name = "certificate_id")
    private Long certificateId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "issued_at", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate issuedAt;


    @Column(name = "certificate_url")
    private String certificateUrl;

    @Column(name = "verification_code", unique = true)
    private String verificationCode;

    @Column(name = "instructor_name")
    private String instructorName;

    @Column(name = "course_name")
    private String courseName;

    @Column(name = "student_name")
    private String studentName;

    @PrePersist
    protected void onCreate() {
        if (issuedAt == null) {
           issuedAt = LocalDate.now();
        }
    }


}

