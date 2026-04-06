package com.edulearn.assessment.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer attemptId;

    @Column(nullable = false)
    private Integer quizId;

    @Column(nullable = false)
    private Integer studentId;

    @Column(nullable = false)
    private Integer score; // 0-100 percentage

    @Column(nullable = false)
    private Boolean passed = false;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime submittedAt;

    private Integer timeTaken; // in seconds

    @Column(columnDefinition = "LONGTEXT")
    private String answers; // JSON string: {"1":"Option A","2":"true","3":"Option B,Option C"}
}

