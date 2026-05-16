package com.edulearn.notification.event;

import org.springframework.context.ApplicationEvent;

public class EnrollmentEvent extends ApplicationEvent {
    private final Long studentId;
    private final Long courseId;
    private final String courseTitle;

    public EnrollmentEvent(Object source, Long studentId, Long courseId, String courseTitle) {
        super(source);
        this.studentId = studentId;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
    }

    public Long getStudentId() {
        return studentId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public String getCourseTitle() {
        return courseTitle;
    }
}
