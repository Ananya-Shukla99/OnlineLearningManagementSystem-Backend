package com.edulearn.notification.event;

import org.springframework.context.ApplicationEvent;

public class CertificateEvent extends ApplicationEvent {
    private int studentId;
    private String courseName;
    private String verificationCode;

    public CertificateEvent(Object source, int studentId, String courseName, String verificationCode) {
        super(source);
        this.studentId = studentId;
        this.courseName = courseName;
        this.verificationCode = verificationCode;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getVerificationCode() {
        return verificationCode;
    }
}

