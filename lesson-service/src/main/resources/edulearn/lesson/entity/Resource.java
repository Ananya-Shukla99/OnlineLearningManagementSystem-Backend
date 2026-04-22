package com.edulearn.lesson.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resources")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer resourceId;

    @Column(nullable = false)
    private Integer lessonId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    private String fileType; // "PDF", "SLIDES", "CODE"

    @Column(nullable = false)
    private Long sizeKb;
}
