package com.edulearn.enrollment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long enrollmentId;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private LocalDateTime enrolledAt;

    @Column(nullable = true)
    private LocalDateTime completedAt;

    @Column(nullable = false)
    private String status; // "ACTIVE", "COMPLETED", "CANCELLED"

    @Column(nullable = false)
    private Integer progressPercent = 0;

    @Column(nullable = false)
    private Boolean certificateIssued = false;
}

