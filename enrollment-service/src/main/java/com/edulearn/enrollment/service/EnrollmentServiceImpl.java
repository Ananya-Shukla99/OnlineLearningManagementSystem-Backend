package com.edulearn.enrollment.service;

import com.edulearn.enrollment.entity.Enrollment;
import com.edulearn.enrollment.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public Enrollment enroll(Long studentId, Integer courseId) {
        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new RuntimeException("Student already enrolled in this course");
        }

        Enrollment enrollment = Enrollment.builder()
                .studentId(studentId)
                .courseId(courseId)
                .enrolledAt(LocalDateTime.now())
                .status("ACTIVE")
                .progressPercent(0)
                .certificateIssued(false)
                .build();

        return enrollmentRepository.save(enrollment);
    }

    @Override
    @Transactional
    public void unenroll(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with ID: " + enrollmentId));

        enrollment.setStatus("CANCELLED");
        enrollmentRepository.save(enrollment);
    }

    @Override
    public List<Enrollment> getEnrollmentsByStudent(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

    @Override
    public List<Enrollment> getEnrollmentsByCourse(Integer courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }

    @Override
    @Transactional
    public void updateProgress(Long studentId, Integer courseId, Integer progressPercent) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        enrollment.setProgressPercent(progressPercent);

        if (progressPercent == 100) {
            enrollment.setStatus("COMPLETED");
            enrollment.setCompletedAt(LocalDateTime.now());
        }

        enrollmentRepository.save(enrollment);
    }

    @Override
    @Transactional
    public void markComplete(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with ID: " + enrollmentId));

        enrollment.setStatus("COMPLETED");
        enrollment.setCompletedAt(LocalDateTime.now());
        enrollment.setProgressPercent(100);
        enrollmentRepository.save(enrollment);
    }

    @Override
    public boolean isEnrolled(Long studentId, Integer courseId) {
        return enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId);
    }

    @Override
    public int getEnrollmentCount(Integer courseId) {
        return enrollmentRepository.countByCourseId(courseId);
    }

}

