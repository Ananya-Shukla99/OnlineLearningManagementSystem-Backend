package com.edulearn.notification.event;

import org.springframework.context.ApplicationEvent;

public class PaymentEvent extends ApplicationEvent {
    private final Long studentId;
    private final double amount;
    private final String courseTitle;

    public PaymentEvent(Object source, Long studentId, double amount, String courseTitle) {
        super(source);
        this.studentId = studentId;
        this.amount = amount;
        this.courseTitle = courseTitle;
    }

    public Long getStudentId() {
        return studentId;
    }

    public double getAmount() {
        return amount;
    }

    public String getCourseTitle() {
        return courseTitle;
    }
}
