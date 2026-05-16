package com.edulearn.progress.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import com.edulearn.progress.entity.Progress;
import com.edulearn.progress.entity.Certificate;
import com.edulearn.progress.pdf.CertificatePdfGenerator;
import com.edulearn.progress.pdf.CertificatePdfTemplate;
import com.edulearn.progress.repository.ProgressRepository;
import com.edulearn.progress.repository.CertificateRepository;
import com.edulearn.notification.event.CertificateEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.edulearn.notification.dto.NotificationDto;
import com.edulearn.notification.config.RabbitMQConfig;

/**
 * Implementation of ProgressService
 * Handles progress tracking and certificate generation with PDFBox
 */
@Service
public class ProgressServiceImpl implements ProgressService {
    private static final Logger log = LoggerFactory.getLogger(ProgressServiceImpl.class);

    private final ProgressRepository progressRepository;
    private final CertificateRepository certificateRepository;
    private final CertificatePdfGenerator certificatePdfGenerator;
    private final RestTemplate restTemplate;
    private final RabbitTemplate rabbitTemplate;

    @Value("${lesson.service.url}")
    private String lessonServiceUrl;

    public ProgressServiceImpl(ProgressRepository progressRepository,
                               CertificateRepository certificateRepository,
                               CertificatePdfGenerator certificatePdfGenerator,
                               RestTemplate restTemplate,
                               RabbitTemplate rabbitTemplate) {
        this.progressRepository = progressRepository;
        this.certificateRepository = certificateRepository;
        this.certificatePdfGenerator = certificatePdfGenerator;
        this.restTemplate = restTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    @Transactional
    public void trackProgress(Long studentId, Long courseId, Long lessonId, Integer watchedSeconds) {
        Optional<Progress> progressOpt = progressRepository.findByStudentIdAndLessonId(studentId, lessonId);

        Progress progress;
        if (progressOpt.isPresent()) {
            progress = progressOpt.get();
            if (watchedSeconds > progress.getWatchedSeconds()) {
                progress.setWatchedSeconds(watchedSeconds);
            }
        } else {
            progress = Progress.builder()
                    .studentId(studentId)
                    .courseId(courseId)
                    .lessonId(lessonId)
                    .watchedSeconds(watchedSeconds)
                    .isCompleted(false)
                    .build();
        }

        progress.setLastAccessedAt(LocalDateTime.now());
        progressRepository.save(progress);
    }

    @Override
    @Transactional
    public Map<String, Object> markLessonComplete(Long studentId, Long courseId, Long lessonId) {
        Optional<Progress> progressOpt = progressRepository.findByStudentIdAndLessonId(studentId, lessonId);

        Progress progress;
        if (progressOpt.isPresent()) {
            progress = progressOpt.get();
        } else {
            progress = Progress.builder()
                    .studentId(studentId)
                    .courseId(courseId)
                    .lessonId(lessonId)
                    .watchedSeconds(0)
                    .isCompleted(true)
                    .build();
        }

        progress.setIsCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());
        progressRepository.save(progress);

        // Check if course is now fully complete and auto-issue certificate
        Integer courseProgress = getCourseProgress(studentId, courseId);
        Map<String, Object> result = new HashMap<>();
        result.put("lessonCompleted", true);
        result.put("courseProgress", courseProgress);

        if (courseProgress >= 100) {
            result.put("courseCompleted", true);
            try {
                Certificate certificate = issueCertificate(studentId, courseId);
                result.put("certificateIssued", true);
                result.put("certificate", certificate);
            } catch (Exception e) {
                // Certificate already issued or generation failed
                Optional<Certificate> existingCert = certificateRepository.findByStudentIdAndCourseId(studentId,
                        courseId);
                if (existingCert.isPresent()) {
                    result.put("certificateIssued", true);
                    result.put("certificate", existingCert.get());
                } else {
                    result.put("certificateIssued", false);
                    result.put("certificateError", e.getMessage());
                }
            }
        } else {
            result.put("courseCompleted", false);
        }

        // SYNC: Notify enrollment-service about the new progress percentage
        try {
            // enrollment-service is on 8084
            String enrollmentUrl = "http://localhost:8084/api/v1/enrollments/progress";
            Map<String, Object> syncReq = new HashMap<>();
            syncReq.put("studentId", studentId);
            syncReq.put("courseId", courseId);
            syncReq.put("progressPercent", courseProgress);
            restTemplate.put(enrollmentUrl, syncReq);
        } catch (Exception e) {
            log.error("Failed to sync progress to Enrollment Service: {}", e.getMessage());
        }

        return result;
    }

    @Override
    public Integer getCourseProgress(Long studentId, Long courseId) {
        int completedLessons = progressRepository.countByStudentIdAndCourseIdAndIsCompleted(
                studentId, courseId, true);

        // Fetch actual total lessons from lesson-service via REST
        int totalLessons = 0;
        try {
            Integer count = restTemplate.getForObject(
                    "http://localhost:8083" + "/api/v1/lessons/count/" + courseId, Integer.class);
            totalLessons = (count != null && count > 0) ? count : 0;
        } catch (Exception e) {
            log.error("Could not reach lesson-service: {}", e.getMessage());
        }

        Integer percentage = 0;
        if (totalLessons > 0) {
            percentage = (completedLessons * 100) / totalLessons;
        }

        // FALLBACK: If local progress is low, check enrollment-service
        if (percentage < 100) {
            try {
                Map<?, ?> enrollRes = restTemplate.getForObject(
                        "http://localhost:8084/api/v1/enrollments/student/" + studentId, Map.class);
                if (enrollRes != null && enrollRes.containsKey("data")) {
                    List<?> list = (List<?>) enrollRes.get("data");
                    for (Object obj : list) {
                        Map<?, ?> e = (Map<?, ?>) obj;
                        // Use a robust way to compare IDs (handle Integer vs Long)
                        Long enrollCourseId = e.get("courseId") != null ? Long.valueOf(e.get("courseId").toString())
                                : null;
                        if (Objects.equals(enrollCourseId, courseId)) {
                            Integer enrollPct = (Integer) e.get("progressPercent");
                            if (enrollPct != null && enrollPct > percentage) {
                                percentage = enrollPct;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Could not reach enrollment-service for fallback: {}", e.getMessage());
            }
        }

        return Math.min(percentage, 100);
    }

    @Override
    public Optional<Progress> getLessonProgress(Long studentId, Long lessonId) {
        return progressRepository.findByStudentIdAndLessonId(studentId, lessonId);
    }

    @Override
    @Transactional
    public Certificate issueCertificate(Long studentId, Long courseId) {
        // 1. Check if certificate already exists
        if (certificateRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            log.debug("Certificate already exists for Student: {}, Course: {}", studentId, courseId);
            return certificateRepository.findByStudentIdAndCourseId(studentId, courseId).orElse(null);
        }
        
        log.debug("Issuing NEW certificate for Student: {}, Course: {}", studentId, courseId);



        // 2. Validate completion (Check granular progress first, then fallback to
        // enrollment service)
        Integer completionPercent = getCourseProgress(studentId, courseId);

        if (completionPercent < 100) {
            log.debug("Granular progress is only {}%. Checking enrollment-service fallback...", completionPercent);
            try {
                // Fallback: Check enrollment-service directly (e.g. for manual completions)
                String enrollmentUrl = "http://localhost:8084/api/v1/enrollments/student/" + studentId;
                Map<String, Object> response = restTemplate.getForObject(enrollmentUrl, Map.class);
                List<?> enrollments = null;
                if (response != null && response.containsKey("data")) {
                    enrollments = (List<?>) response.get("data");
                }

                boolean foundComplete = false;
                if (enrollments != null) {
                    for (Object obj : enrollments) {
                        Map<?, ?> e = (Map<?, ?>) obj;
                        if (String.valueOf(e.get("courseId")).equals(String.valueOf(courseId))) {
                            Object pctObj = e.get("progressPercent");
                            if (pctObj != null && ((Number) pctObj).intValue() >= 100) {
                                foundComplete = true;
                                break;
                            }
                        }
                    }
                }
                if (!foundComplete) {
                    log.debug("Enrollment check FAILED for user {} - Course {} is NOT 100%", studentId, courseId);
                    throw new RuntimeException("Course not yet fully completed (Enrollment says not 100%)");
                }
                log.debug("Enrollment fallback confirmed 100% for user {}. Proceeding with certificate generation.", studentId);

            } catch (Exception e) {
                if (e.getMessage().contains("Course not yet fully completed"))
                    throw (RuntimeException) e;
                log.error("Failed to verify completion via enrollment-service: {}", e.getMessage());
                throw new RuntimeException("Course not yet fully completed and enrollment service unreachable");
            }
        }

        String verificationCode = generateVerificationCode(courseId);

        // Try to fetch course name from course-service
        String courseName = "Course " + courseId;
        String studentName = "Student #" + studentId;
        try {
            // Course name fetch
            Map<?, ?> courseRes = restTemplate.getForObject(
                    "http://localhost:8082" + "/api/v1/courses/" + courseId, Map.class);
            if (courseRes != null && courseRes.containsKey("data")) {
                Map<?, ?> data = (Map<?, ?>) courseRes.get("data");
                if (data != null && data.containsKey("title")) {
                    courseName = String.valueOf(data.get("title"));
                }
            }

            // Student name fetch from auth-service (Port 8081)
            Map<?, ?> userRes = restTemplate.getForObject(
                    "http://localhost:8081/auth/user/" + studentId, Map.class);
            
            if (userRes != null && userRes.get("data") != null) {
                Map<?, ?> data = (Map<?, ?>) userRes.get("data");
                if (data.get("fullName") != null) {
                    studentName = String.valueOf(data.get("fullName"));
                } else if (data.get("name") != null) {
                    studentName = String.valueOf(data.get("name"));
                }
            }
        } catch (Exception e) {
            log.error("Side-fetch for student name failed: {}", e.getMessage());
            // Fallback: If direct service call fails, try Gateway (Port 8080)
            try {
                Map<?, ?> userRes = restTemplate.getForObject("http://localhost:8080/auth/user/" + studentId, Map.class);
                if (userRes != null && userRes.get("data") != null) {
                    Map<?, ?> data = (Map<?, ?>) userRes.get("data");
                    if (data.get("fullName") != null) studentName = String.valueOf(data.get("fullName"));
                }
            } catch (Exception e2) {
                log.error("Gateway fetch also failed: {}", e2.getMessage());
            }
        }

        CertificatePdfTemplate pdfTemplate = CertificatePdfTemplate.random();
        String pdfPath = certificatePdfGenerator.generate(pdfTemplate, studentId, courseId, verificationCode, courseName,
                studentName);
        String fileName = new File(pdfPath).getName();

        Certificate certificate = Certificate.builder()
                .studentId(studentId)
                .courseId(courseId)
                .issuedAt(LocalDate.now())
                .certificateUrl("/api/v1/progress/certificates/download/" + fileName)
                .verificationCode(verificationCode)
                .instructorName("Course Instructor")
                .courseName(courseName)
                .studentName(studentName)
                .certificateFormat(pdfTemplate.name())
                .build();

        Certificate savedCertificate = certificateRepository.save(certificate);

        // Send RabbitMQ Notification for Certificate
        try {
            NotificationDto certNotif = new NotificationDto();
            certNotif.setUserId(studentId);
            certNotif.setType("CERTIFICATE");
            certNotif.setTitle("Certificate Earned!");
            certNotif.setMessage("Congratulations! Your certificate for '" + courseName + "' has been issued. Verification code: " + verificationCode);
            certNotif.setRelatedEntityId(courseId);
            certNotif.setRelatedEntityType("CERTIFICATE");
            
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, certNotif);
            log.info("Certificate notification sent to RabbitMQ for student: {}", studentId);
        } catch (Exception e) {
            log.error("Failed to send RabbitMQ certificate notification: {}", e.getMessage());
        }

        return savedCertificate;
    }

    private String generateVerificationCode(Long courseId) {
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String categoryPrefix = "EL";
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        return "EL-" + year + "-" + categoryPrefix + "-" + randomPart;
    }

    @Override
    public Optional<Certificate> getCertificate(Long studentId, Long courseId) {
        return certificateRepository.findByStudentIdAndCourseId(studentId, courseId);
    }

    @Override
    public Optional<Certificate> verifyCertificate(String verificationCode) {
        return certificateRepository.findByVerificationCode(verificationCode);
    }

    @Override
    public List<Progress> getAllProgressByStudent(Long studentId) {
        return progressRepository.findByStudentId(studentId);
    }

    @Override
    public List<Certificate> getAllCertificatesByStudent(Long studentId) {
        return certificateRepository.findByStudentId(studentId);
    }
}
