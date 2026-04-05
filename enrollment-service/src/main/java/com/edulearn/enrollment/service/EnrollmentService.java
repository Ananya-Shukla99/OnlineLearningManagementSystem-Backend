package com.edulearn.enrollment.service;

import com.edulearn.enrollment.entity.Enrollment;
import java.util.List;

public interface EnrollmentService {

    Enrollment enroll(Long studentId, Integer courseId);

    void unenroll(Long enrollmentId);

    List<Enrollment> getEnrollmentsByStudent(Long studentId);

    List<Enrollment> getEnrollmentsByCourse(Integer courseId);

    void updateProgress(Long studentId, Integer courseId, Integer progressPercent);

    void markComplete(Long enrollmentId);

    boolean isEnrolled(Long studentId, Integer courseId);

    int getEnrollmentCount(Integer courseId);
}

