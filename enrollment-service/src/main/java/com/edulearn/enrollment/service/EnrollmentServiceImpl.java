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
        Map<String, Object> courseData = getCourseDetails(courseId);
        if (courseData == null) {
            throw new RuntimeException("Course not found");
        }
        
        Object isPublishedObj = courseData.get("isPublished");
        if (isPublishedObj == null) isPublishedObj = courseData.get("published");
        if (!Boolean.TRUE.equals(isPublishedObj)) {
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
            
            // Notify Admin
            NotificationDto adminNotif = new NotificationDto();
            adminNotif.setUserId(1L);
            adminNotif.setType("NEW_ENROLLMENT_ADMIN");
            adminNotif.setTitle("New Student Enrollment");
            adminNotif.setMessage("A student (ID: " + studentId + ") has enrolled in course ID: " + courseId);
            adminNotif.setRelatedEntityId(courseId);
            adminNotif.setRelatedEntityType("COURSE");
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, adminNotif);
            System.out.println("Notification sent to RabbitMQ for admin");

            // Notify Instructor
            Object instructorIdObj = courseData.get("instructorId");
            Object courseTitleObj = courseData.get("title");
            if (instructorIdObj != null) {
                Long instructorId = Long.valueOf(instructorIdObj.toString());
                String courseTitle = courseTitleObj != null ? courseTitleObj.toString() : "Course " + courseId;
                
                NotificationDto instructorNotif = new NotificationDto();
                instructorNotif.setUserId(instructorId);
                instructorNotif.setType("NEW_ENROLLMENT_INSTRUCTOR");
                instructorNotif.setTitle("New Student Enrolled!");
                instructorNotif.setMessage("A student has just enrolled in your course: '" + courseTitle + "'.");
                instructorNotif.setRelatedEntityId(courseId);
                instructorNotif.setRelatedEntityType("COURSE");
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, instructorNotif);
                System.out.println("Notification sent to RabbitMQ for instructor: " + instructorId);
            }
        } catch (Exception e) {
            System.err.println("Failed to send RabbitMQ notification: " + e.getMessage());
        }

        return saved;
    }

    /**
     * Fetch course details via Feign Client (inter-service communication).
     */
    private Map<String, Object> getCourseDetails(Long courseId) {
        try {
            Map<String, Object> response = courseClient.getCourse(courseId);
            Object data = response.get("data");
            if (data instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapData = (Map<String, Object>) data;
                return mapData;
            }
        } catch (Exception ex) {
            System.err.println("Error verifying course status via Feign: " + ex.getMessage());
        }
        return null;
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
