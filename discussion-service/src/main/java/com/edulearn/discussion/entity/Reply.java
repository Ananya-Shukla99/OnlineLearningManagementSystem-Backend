package com.edulearn.discussion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "replies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer replyId;

    private Integer threadId;

    private Integer authorId;
    private String authorName;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "is_accepted", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isAccepted;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer upvotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isAccepted == null) {
            this.isAccepted = false;
        }
        if (this.upvotes == null) {
            this.upvotes = 0;
        }
    }
}

