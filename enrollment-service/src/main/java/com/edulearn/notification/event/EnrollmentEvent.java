package com.edulearn.notification.event;

import org.springframework.context.ApplicationEvent;

public class EnrollmentEvent extends ApplicationEvent {
    private int studentId;
    private int courseId;
    private String courseTitle;

    public EnrollmentEvent(Object source, int studentId, int courseId, String courseTitle) {
        super(source);
        this.studentId = studentId;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
    }

    public int getStudentId() {
        return studentId;
    }

    public int getCourseId() {
        return courseId;
    }

    public String getCourseTitle() {
        return courseTitle;
    }
}

