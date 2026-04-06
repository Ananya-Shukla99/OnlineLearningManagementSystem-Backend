package com.edulearn.assessment.entity;

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
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer questionId;

    @Column(nullable = false)
    private Integer quizId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(nullable = false, length = 50)
    private String questionType; // "MCQ_SINGLE", "MCQ_MULTI", "TRUE_FALSE"

    @Column(columnDefinition = "TEXT")
    private String options; // comma-separated: "Option A,Option B,Option C,Option D"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String correctAnswer; // For MCQ_SINGLE: "Option A"
                                  // For MCQ_MULTI: "Option A,Option B"
                                  // For TRUE_FALSE: "true" or "false"

    @Column(nullable = false)
    private Integer marks; // e.g., 1, 2, 5 points

    @Column(nullable = false)
    private Integer orderIndex; // Display order in quiz (1, 2, 3...)
}

