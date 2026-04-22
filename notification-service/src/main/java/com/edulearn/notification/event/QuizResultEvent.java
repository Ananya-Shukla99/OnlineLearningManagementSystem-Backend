package com.edulearn.notification.event;

import org.springframework.context.ApplicationEvent;

public class QuizResultEvent extends ApplicationEvent {
    private final Long studentId;
    private final String quizTitle;
    private final int score;
    private final boolean passed;

    public QuizResultEvent(Object source, Long studentId, String quizTitle, int score, boolean passed) {
        super(source);
        this.studentId = studentId;
        this.quizTitle = quizTitle;
        this.score = score;
        this.passed = passed;
    }

    public Long getStudentId() {
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
