package com.edulearn.lesson.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lessons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer lessonId;

    @Column(nullable = false)
    private Integer courseId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String contentType; // "VIDEO", "ARTICLE", "PDF"

    @Column(nullable = false)
    private String contentUrl;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false)
    private Integer orderIndex;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean isPreview = false;
}
