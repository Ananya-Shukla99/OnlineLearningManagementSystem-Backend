package com.edulearn.notification.event;

import org.springframework.context.ApplicationEvent;

public class QuizResultEvent extends ApplicationEvent {
    private int studentId;
    private String quizTitle;
    private int score;
    private boolean passed;

    public QuizResultEvent(Object source, int studentId, String quizTitle, int score, boolean passed) {
        super(source);
        this.studentId = studentId;
        this.quizTitle = quizTitle;
        this.score = score;
        this.passed = passed;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public int getScore() {
        return score;
    }

    public boolean isPassed() {
        return passed;
    }
}

