package com.edulearn.progress.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.edulearn.progress.entity.Progress;
import com.edulearn.progress.entity.Certificate;
import com.edulearn.progress.repository.ProgressRepository;
import com.edulearn.progress.repository.CertificateRepository;
import com.edulearn.progress.service.ProgressService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration tests for the complete Progress Service
 * Tests the full flow of progress tracking, course completion, and certificate generation
 * Includes repository, service, and controller layers
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Progress Service Integration Test Suite")
class ProgressServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private ProgressService progressService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up before each test
        certificateRepository.deleteAll();
        progressRepository.deleteAll();
    }

    // ============================================
    // Single Lesson Progress Tests
    // ============================================

    @Test
    @DisplayName("Should track single lesson progress and retrieve it")
    @Transactional
    void testTrackAndRetrieveSingleLessonProgress() {
        // Arrange & Act
        progressService.trackProgress(1, 1, 1, 300);

        // Assert - Verify via service
        Optional<Progress> progress = progressService.getLessonProgress(1, 1);
        assertTrue(progress.isPresent());
        assertEquals(300, progress.get().getWatchedSeconds());
        assertFalse(progress.get().getIsCompleted());

        // Verify via repository
        Optional<Progress> dbProgress = progressRepository.findByStudentIdAndLessonId(1, 1);
        assertTrue(dbProgress.isPresent());
        assertEquals(1, dbProgress.get().getStudentId());
        assertEquals(1, dbProgress.get().getLessonId());
    }

    @Test
    @DisplayName("Should update lesson progress when watched seconds increase")
    @Transactional
    void testUpdateLessonProgressWatchedSeconds() {
        // Arrange
        progressService.trackProgress(1, 1, 1, 300);

        // Act - Update with more watched seconds
        progressService.trackProgress(1, 1, 1, 600);

        // Assert
        Optional<Progress> progress = progressService.getLessonProgress(1, 1);
        assertTrue(progress.isPresent());
        assertEquals(600, progress.get().getWatchedSeconds());
    }

    @Test
    @DisplayName("Should not decrease watched seconds when lower value provided")
    @Transactional
    void testWatchedSecondsNotDecreased() {
        // Arrange
        progressService.trackProgress(1, 1, 1, 600);

        // Act - Attempt to update with lower value
        progressService.trackProgress(1, 1, 1, 300);

        // Assert - Should remain 600
        Optional<Progress> progress = progressService.getLessonProgress(1, 1);
        assertTrue(progress.isPresent());
        assertEquals(600, progress.get().getWatchedSeconds());
    }

    // ============================================
    // Multiple Lessons Progress Tests
    // ============================================

    @Test
    @DisplayName("Should track progress for multiple lessons in a course")
    @Transactional
    void testTrackMultipleLessonsInCourse() {
        // Arrange & Act
        for (int i = 1; i <= 3; i++) {
            progressService.trackProgress(1, 1, i, 100 * i);
        }

        // Assert
        List<Progress> courseProgress = progressRepository.findByStudentIdAndCourseId(1, 1);
        assertEquals(3, courseProgress.size());
        assertTrue(courseProgress.stream().allMatch(p -> p.getStudentId() == 1 && p.getCourseId() == 1));
    }

    @Test
    @DisplayName("Should retrieve all progress for student across multiple courses")
    @Transactional
    void testGetAllProgressAcrossCourses() {
        // Arrange & Act
        // Course 1
        progressService.trackProgress(1, 1, 1, 300);
        progressService.trackProgress(1, 1, 2, 400);

        // Course 2
        progressService.trackProgress(1, 2, 1, 200);
        progressService.trackProgress(1, 2, 2, 500);

        // Assert
        List<Progress> allProgress = progressService.getAllProgressByStudent(1);
        assertEquals(4, allProgress.size());

        List<Progress> course1Progress = progressRepository.findByStudentIdAndCourseId(1, 1);
        assertEquals(2, course1Progress.size());

        List<Progress> course2Progress = progressRepository.findByStudentIdAndCourseId(1, 2);
        assertEquals(2, course2Progress.size());
    }

    // ============================================
    // Course Completion Tests
    // ============================================

    @Test
    @DisplayName("Should mark lessons as complete and calculate 100% progress")
    @Transactional
    void testCompleteCourseAndGetProgress() {
        // Arrange & Act - Mark all 3 lessons as complete
        progressService.markLessonComplete(1, 1, 1);
        progressService.markLessonComplete(1, 1, 2);
        progressService.markLessonComplete(1, 1, 3);

        // Assert
        Integer courseProgress = progressService.getCourseProgress(1, 1);
        assertEquals(100, courseProgress);

        // Verify all are marked complete
        int completedCount = progressRepository.countByStudentIdAndCourseIdAndIsCompleted(1, 1, true);
        assertEquals(3, completedCount);
    }

    @Test
    @DisplayName("Should calculate partial progress for incomplete course")
    @Transactional
    void testPartialCourseProgress() {
        // Arrange & Act - Mark only 1 of 3 lessons as complete
        progressService.markLessonComplete(1, 1, 1);

        // Assert
        Integer courseProgress = progressService.getCourseProgress(1, 1);
        assertEquals(33, courseProgress);

        int completedCount = progressRepository.countByStudentIdAndCourseIdAndIsCompleted(1, 1, true);
        assertEquals(1, completedCount);
    }

    @Test
    @DisplayName("Should return 0% progress for course with no completed lessons")
    @Transactional
    void testZeroCourseProgress() {
        // Arrange - Track progress but don't complete
        progressService.trackProgress(1, 1, 1, 300);
        progressService.trackProgress(1, 1, 2, 400);

        // Act
        Integer courseProgress = progressService.getCourseProgress(1, 1);

        // Assert
        assertEquals(0, courseProgress);
    }

    // ============================================
    // Certificate Issuance Tests
    // ============================================

    @Test
    @DisplayName("Should issue certificate when course is completed")
    @Transactional
    void testIssueCertificateForCompletedCourse() {
        // Arrange - Complete the course
        progressService.markLessonComplete(1, 1, 1);
        progressService.markLessonComplete(1, 1, 2);
        progressService.markLessonComplete(1, 1, 3);

        // Act
        Certificate certificate = progressService.issueCertificate(1, 1);

        // Assert
        assertNotNull(certificate);
        assertNotNull(certificate.getCertificateId());
        assertEquals(1, certificate.getStudentId());
        assertEquals(1, certificate.getCourseId());
        assertNotNull(certificate.getVerificationCode());
        assertTrue(certificate.getVerificationCode().startsWith("EL-"));
        assertNotNull(certificate.getCertificateUrl());
    }

    @Test
    @DisplayName("Should return existing certificate if already issued")
    @Transactional
    void testIssueCertificateAlreadyExists() {
        // Arrange - Issue certificate first time
        Certificate cert1 = progressService.issueCertificate(1, 1);

        // Act - Try to issue again
        Certificate cert2 = progressService.issueCertificate(1, 1);

        // Assert - Should return same certificate
        assertEquals(cert1.getCertificateId(), cert2.getCertificateId());
        assertEquals(cert1.getVerificationCode(), cert2.getVerificationCode());
    }

    @Test
    @DisplayName("Should generate unique verification codes for different certificates")
    @Transactional
    void testUniqueVerificationCodes() {
        // Arrange & Act
        Certificate cert1 = progressService.issueCertificate(1, 1);
        Certificate cert2 = progressService.issueCertificate(1, 2);
        Certificate cert3 = progressService.issueCertificate(2, 1);

        // Assert
        assertNotEquals(cert1.getVerificationCode(), cert2.getVerificationCode());
        assertNotEquals(cert2.getVerificationCode(), cert3.getVerificationCode());
        assertNotEquals(cert1.getVerificationCode(), cert3.getVerificationCode());
    }

    // ============================================
    // Certificate Verification Tests
    // ============================================

    @Test
    @DisplayName("Should verify certificate by verification code")
    @Transactional
    void testVerifyCertificateByCode() {
        // Arrange - Issue certificate
        Certificate issuedCert = progressService.issueCertificate(1, 1);
        String verificationCode = issuedCert.getVerificationCode();

        // Act
        Optional<Certificate> verifiedCert = progressService.verifyCertificate(verificationCode);

        // Assert
        assertTrue(verifiedCert.isPresent());
        assertEquals(issuedCert.getCertificateId(), verifiedCert.get().getCertificateId());
        assertEquals(issuedCert.getStudentId(), verifiedCert.get().getStudentId());
        assertEquals(issuedCert.getCourseId(), verifiedCert.get().getCourseId());
    }

    @Test
    @DisplayName("Should return empty when verification code is invalid")
    @Transactional
    void testVerifyInvalidCertificate() {
        // Act
        Optional<Certificate> verifiedCert = progressService.verifyCertificate("INVALID-CODE-XYZ");

        // Assert
        assertFalse(verifiedCert.isPresent());
    }

    @Test
    @DisplayName("Should retrieve all certificates for a student")
    @Transactional
    void testGetAllCertificatesForStudent() {
        // Arrange & Act - Issue multiple certificates
        progressService.issueCertificate(1, 1);
        progressService.issueCertificate(1, 2);
        progressService.issueCertificate(1, 3);

        // Act
        List<Certificate> certificates = progressService.getAllCertificatesByStudent(1);

        // Assert
        assertEquals(3, certificates.size());
        assertTrue(certificates.stream().allMatch(c -> c.getStudentId() == 1));
    }

    // ============================================
    // Multiple Students Tests
    // ============================================

    @Test
    @DisplayName("Should maintain separate progress for different students")
    @Transactional
    void testProgressIsolationBetweenStudents() {
        // Arrange & Act
        // Student 1
        progressService.trackProgress(1, 1, 1, 300);
        progressService.markLessonComplete(1, 1, 1);

        // Student 2
        progressService.trackProgress(2, 1, 1, 200);

        // Assert
        List<Progress> student1Progress = progressService.getAllProgressByStudent(1);
        List<Progress> student2Progress = progressService.getAllProgressByStudent(2);

        assertEquals(1, student1Progress.size());
        assertEquals(1, student2Progress.size());

        assertEquals(300, student1Progress.get(0).getWatchedSeconds());
        assertEquals(200, student2Progress.get(0).getWatchedSeconds());

        assertTrue(student1Progress.get(0).getIsCompleted());
        assertFalse(student2Progress.get(0).getIsCompleted());
    }

    @Test
    @DisplayName("Should maintain separate certificates for different students")
    @Transactional
    void testCertificateIsolationBetweenStudents() {
        // Arrange & Act
        Certificate cert1 = progressService.issueCertificate(1, 1);
        Certificate cert2 = progressService.issueCertificate(2, 1);

        List<Certificate> student1Certs = progressService.getAllCertificatesByStudent(1);
        List<Certificate> student2Certs = progressService.getAllCertificatesByStudent(2);

        // Assert
        assertEquals(1, student1Certs.size());
        assertEquals(1, student2Certs.size());
        assertEquals(1, student1Certs.get(0).getStudentId());
        assertEquals(2, student2Certs.get(0).getStudentId());
    }

    // ============================================
    // REST Endpoint Integration Tests
    // ============================================

    @Test
    @DisplayName("Should track progress via REST endpoint")
    @Transactional
    void testTrackProgressViaREST() throws Exception {
        // Arrange
        Map<String, Integer> request = new HashMap<>();
        request.put("studentId", 1);
        request.put("courseId", 1);
        request.put("lessonId", 1);
        request.put("watchedSeconds", 300);

        // Act & Assert
        mockMvc.perform(put("/api/v1/progress/track")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify in database
        Optional<Progress> progress = progressRepository.findByStudentIdAndLessonId(1, 1);
        assertTrue(progress.isPresent());
        assertEquals(300, progress.get().getWatchedSeconds());
    }

    @Test
    @DisplayName("Should complete course and issue certificate via REST")
    @Transactional
    void testCompleteCourseAndIssueCertificateViaREST() throws Exception {
        // Arrange - Complete all lessons
        Map<String, Integer> completeRequest = new HashMap<>();
        completeRequest.put("studentId", 1);
        completeRequest.put("courseId", 1);

        for (int i = 1; i <= 3; i++) {
            completeRequest.put("lessonId", i);
            mockMvc.perform(put("/api/v1/progress/complete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(completeRequest)))
                    .andExpect(status().isOk());
        }

        // Act - Issue certificate
        mockMvc.perform(post("/api/v1/progress/certificates/issue")
                .param("studentId", "1")
                .param("courseId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.certificateId").isNumber())
                .andExpect(jsonPath("$.verificationCode").isString())
                .andExpect(jsonPath("$.studentId").value(1))
                .andExpect(jsonPath("$.courseId").value(1));

        // Verify in database
        List<Certificate> certificates = certificateRepository.findByStudentId(1);
        assertEquals(1, certificates.size());
    }

    @Test
    @DisplayName("Should complete workflow: track -> get progress -> complete -> issue certificate")
    @Transactional
    void testCompleteWorkflowViaREST() throws Exception {
        // Step 1: Track progress for all lessons
        for (int i = 1; i <= 3; i++) {
            Map<String, Integer> trackRequest = new HashMap<>();
            trackRequest.put("studentId", 1);
            trackRequest.put("courseId", 1);
            trackRequest.put("lessonId", i);
            trackRequest.put("watchedSeconds", 100 * i);

            mockMvc.perform(put("/api/v1/progress/track")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(trackRequest)))
                    .andExpect(status().isOk());
        }

        // Step 2: Get course progress (should be 0%)
        mockMvc.perform(get("/api/v1/progress/course")
                .param("studentId", "1")
                .param("courseId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json("0"));

        // Step 3: Complete all lessons
        Map<String, Integer> completeRequest = new HashMap<>();
        completeRequest.put("studentId", 1);
        completeRequest.put("courseId", 1);

        for (int i = 1; i <= 3; i++) {
            completeRequest.put("lessonId", i);
            mockMvc.perform(put("/api/v1/progress/complete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(completeRequest)))
                    .andExpect(status().isOk());
        }

        // Step 4: Get course progress (should be 100%)
        mockMvc.perform(get("/api/v1/progress/course")
                .param("studentId", "1")
                .param("courseId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json("100"));

        // Step 5: Issue certificate
        mockMvc.perform(post("/api/v1/progress/certificates/issue")
                .param("studentId", "1")
                .param("courseId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.certificateId").isNumber())
                .andExpect(jsonPath("$.verificationCode").exists());

        // Step 6: Get student certificates
        mockMvc.perform(get("/api/v1/progress/certificates/student/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].studentId").value(1));

        // Step 7: Verify certificate (via public endpoint)
        String verificationCode = certificateRepository.findByStudentId(1).get(0).getVerificationCode();
        mockMvc.perform(get("/api/v1/progress/certificates/verify/{verificationCode}", verificationCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationCode").value(verificationCode))
                .andExpect(jsonPath("$.studentId").value(1));
    }
}

