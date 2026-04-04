package com.edulearn.course.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer courseId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, length = 50)
    private String level; // Beginner, Intermediate, Advanced

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer instructorId; // Stores userId of the instructor

    @Column(length = 500)
    private String thumbnailUrl;

    @Column(nullable = false)
    private Integer totalDuration; // in minutes

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isPublished = false;

    @Column(nullable = false)
    private LocalDate createdAt = LocalDate.now();

    @Column(length = 50)
    private String language;
}

