package com.edulearn.notification.event;

import org.springframework.context.ApplicationEvent;

public class PaymentEvent extends ApplicationEvent {
    private int studentId;
    private double amount;
    private String courseTitle;

    public PaymentEvent(Object source, int studentId, double amount, String courseTitle) {
        super(source);
        this.studentId = studentId;
        this.amount = amount;
        this.courseTitle = courseTitle;
    }

    public int getStudentId() {
        return studentId;
    }

    public double getAmount() {
        return amount;
    }

    public String getCourseTitle() {
        return courseTitle;
    }
}

