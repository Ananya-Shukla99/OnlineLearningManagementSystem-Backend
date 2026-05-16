package com.edulearn.discussion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "upvote_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpvoteRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer upvoteId;

    private Integer replyId;

    private Integer studentId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

