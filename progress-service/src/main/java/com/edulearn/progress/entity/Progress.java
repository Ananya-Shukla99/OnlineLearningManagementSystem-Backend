package com.edulearn.progress.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Progress entity to track student lesson completion
 * Records how much of each lesson a student has watched and completion status
 */
@Entity
@Table(name = "progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer progressId;

    @Column(nullable = false)
    private Integer studentId;

    @Column(nullable = false)
    private Integer courseId;

    @Column(nullable = false)
    private Integer lessonId;

    @Column(name = "watched_seconds", nullable = false)
    private Integer watchedSeconds;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        if (watchedSeconds == null) {
            watchedSeconds = 0;
        }
        if (isCompleted == null) {
            isCompleted = false;
        }
    }
}

