package com.edulearn.enrollment.service;

import com.edulearn.enrollment.client.CourseClient;
import com.edulearn.enrollment.entity.Enrollment;
import com.edulearn.enrollment.repository.EnrollmentRepository;
import com.edulearn.notification.dto.NotificationDto;
import com.edulearn.notification.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private CourseClient courseClient;

    @Override
    @Transactional
    public Enrollment enroll(Long studentId, Long courseId) {
        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new RuntimeException("Student already enrolled in this course");
        }
        if (!isCoursePublished(courseId)) {
            throw new RuntimeException("Cannot enroll in an unpublished course");
        }

        Enrollment enrollment = Enrollment.builder()
                .studentId(studentId)
                .courseId(courseId)
                .enrolledAt(LocalDateTime.now())
                .status("ACTIVE")
                .progressPercent(0)
                .certificateIssued(false)
                .build();

        Enrollment saved = enrollmentRepository.save(enrollment);

        // Trigger Notification via RabbitMQ
        try {
            NotificationDto notification = new NotificationDto();
            notification.setUserId(studentId);
            notification.setType("ENROLLMENT");
            notification.setTitle("Enrolled Successfully!");
            notification.setMessage("You have successfully enrolled in the course. Start learning now!");
            notification.setRelatedEntityId(courseId);
            notification.setRelatedEntityType("COURSE");

            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, notification);
            System.out.println("Notification sent to RabbitMQ for student: " + studentId);
        } catch (Exception e) {
            System.err.println("Failed to send RabbitMQ notification: " + e.getMessage());
        }

        return saved;
    }

    /**
     * Check if a course is published via Feign Client (inter-service communication).
     * Feign resolves "course-service" via Eureka — no hardcoded URLs.
     */
    private boolean isCoursePublished(Long courseId) {
        try {
            Map<String, Object> response = courseClient.getCourse(courseId);
            Object data = response.get("data");
            if (!(data instanceof Map<?, ?> mapData)) return false;
            Object isPublished = mapData.get("isPublished");
            if (isPublished == null) isPublished = mapData.get("published");
            return Boolean.TRUE.equals(isPublished);
        } catch (Exception ex) {
            System.err.println("Error verifying course status via Feign: " + ex.getMessage());
            return false;
        }
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
    public List<Enrollment> getEnrollmentsByCourse(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }

    @Override
    @Transactional
    public void updateProgress(Long studentId, Long courseId, Integer progressPercent) {
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
    public boolean isEnrolled(Long studentId, Long courseId) {
        return enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId);
    }

    @Override
    public int getEnrollmentCount(Long courseId) {
        return enrollmentRepository.countByCourseId(courseId);
    }

}
