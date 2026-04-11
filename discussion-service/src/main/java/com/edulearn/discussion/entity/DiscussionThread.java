package com.edulearn.discussion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "discussion_threads")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionThread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer threadId;

    private Integer courseId;

    private Integer lessonId; // nullable

    private Integer authorId;

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "is_pinned", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isPinned;

    @Column(name = "is_closed", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isClosed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isPinned == null) {
            this.isPinned = false;
        }
        if (this.isClosed == null) {
            this.isClosed = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

