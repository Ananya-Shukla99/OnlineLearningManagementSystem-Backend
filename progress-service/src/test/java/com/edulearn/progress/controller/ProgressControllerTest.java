package com.edulearn.progress.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.edulearn.progress.entity.Progress;
import com.edulearn.progress.entity.Certificate;
import com.edulearn.progress.service.ProgressService;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Integration tests for ProgressController
 * Tests REST endpoints with Spring Security enabled
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("ProgressController Test Suite")
class ProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProgressService progressService;


    @Autowired
    private ObjectMapper objectMapper;

    private Progress testProgress;
    private Certificate testCertificate;
    private Map<String, Integer> trackProgressRequest;

    @BeforeEach
    void setUp() {
        testProgress = Progress.builder()
                .progressId(1)
                .studentId(1)
                .courseId(1)
                .lessonId(1)
                .watchedSeconds(300)
                .isCompleted(false)
                .lastAccessedAt(LocalDateTime.now())
                .build();

        testCertificate = Certificate.builder()
                .certificateId(1)
                .studentId(1)
                .courseId(1)
                .issuedAt(LocalDate.now())
                .certificateUrl("src/main/resources/certificates/cert_1_1.pdf")
                .verificationCode("EL-2026-EL-ABC123")
                .instructorName("Test Instructor")
                .courseName("Test Course")
                .build();

        trackProgressRequest = new HashMap<>();
        trackProgressRequest.put("studentId", 1);
        trackProgressRequest.put("courseId", 1);
        trackProgressRequest.put("lessonId", 1);
        trackProgressRequest.put("watchedSeconds", 300);
    }

    // ============================================
    // Track Progress Endpoint Tests
    // ============================================

    @Test
    @DisplayName("Should track progress successfully")
    @WithMockUser(username = "testuser", roles = "USER")
    void testTrackProgress() throws Exception {
        // Arrange
        doNothing().when(progressService).trackProgress(1, 1, 1, 300);

        // Act & Assert
        mockMvc.perform(put("/api/v1/progress/track")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trackProgressRequest)))
                .andExpect(status().isOk());

        verify(progressService, times(1)).trackProgress(1, 1, 1, 300);
    }

    @Test
    @DisplayName("Should handle multiple progress tracking requests")
    @WithMockUser(username = "testuser", roles = "USER")
    void testTrackProgressMultipleLessons() throws Exception {
        // Arrange
        doNothing().when(progressService).trackProgress(anyInt(), anyInt(), anyInt(), anyInt());

        // Act & Assert
        for (int i = 1; i <= 3; i++) {
            Map<String, Integer> request = new HashMap<>();
            request.put("studentId", 1);
            request.put("courseId", 1);
            request.put("lessonId", i);
            request.put("watchedSeconds", 100 * i);

            mockMvc.perform(put("/api/v1/progress/track")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        verify(progressService, times(3)).trackProgress(anyInt(), anyInt(), anyInt(), anyInt());
    }

    // ============================================
    // Mark Lesson Complete Endpoint Tests
    // ============================================

    @Test
    @DisplayName("Should mark lesson as complete successfully")
    @WithMockUser(username = "testuser", roles = "USER")
    void testMarkLessonComplete() throws Exception {
        // Arrange
        Map<String, Integer> completeRequest = new HashMap<>();
        completeRequest.put("studentId", 1);
        completeRequest.put("courseId", 1);
        completeRequest.put("lessonId", 1);

        doNothing().when(progressService).markLessonComplete(1, 1, 1);

        // Act & Assert
        mockMvc.perform(put("/api/v1/progress/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(completeRequest)))
                .andExpect(status().isOk());

        verify(progressService, times(1)).markLessonComplete(1, 1, 1);
    }

    @Test
    @DisplayName("Should mark multiple lessons as complete")
    @WithMockUser(username = "testuser", roles = "USER")
    void testMarkMultipleLessonsComplete() throws Exception {
        // Arrange
        doNothing().when(progressService).markLessonComplete(anyInt(), anyInt(), anyInt());

        // Act & Assert
        for (int i = 1; i <= 3; i++) {
            Map<String, Integer> request = new HashMap<>();
            request.put("studentId", 1);
            request.put("courseId", 1);
            request.put("lessonId", i);

            mockMvc.perform(put("/api/v1/progress/complete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        verify(progressService, times(3)).markLessonComplete(anyInt(), anyInt(), anyInt());
    }

    // ============================================
    // Get Course Progress Endpoint Tests
    // ============================================

    @Test
    @DisplayName("Should get course progress percentage")
    @WithMockUser(username = "testuser", roles = "USER")
    void testGetCourseProgress() throws Exception {
        // Arrange
        when(progressService.getCourseProgress(1, 1)).thenReturn(50);

        // Act & Assert
        mockMvc.perform(get("/api/v1/progress/course")
                .param("studentId", "1")
                .param("courseId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("50"));

        verify(progressService, times(1)).getCourseProgress(1, 1);
    }

    @Test
    @DisplayName("Should return 0 progress for new course")
    @WithMockUser(username = "testuser", roles = "USER")
    void testGetCourseProgressNewCourse() throws Exception {
        // Arrange
        when(progressService.getCourseProgress(1, 999)).thenReturn(0);

        // Act & Assert
        mockMvc.perform(get("/api/v1/progress/course")
                .param("studentId", "1")
                .param("courseId", "999"))
                .andExpect(status().isOk())
                .andExpect(content().json("0"));
    }

    @Test
    @DisplayName("Should return 100% progress for completed course")
    @WithMockUser(username = "testuser", roles = "USER")
    void testGetCourseProgressCompleted() throws Exception {
        // Arrange
        when(progressService.getCourseProgress(1, 1)).thenReturn(100);

        // Act & Assert
        mockMvc.perform(get("/api/v1/progress/course")
                .param("studentId", "1")
                .param("courseId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json("100"));
    }

    // ============================================
    // Get Lesson Progress Endpoint Tests
    // ============================================

    @Test
    @DisplayName("Should get lesson progress when exists")
    @WithMockUser(username = "testuser", roles = "USER")
    void testGetLessonProgressExists() throws Exception {
        // Arrange
        when(progressService.getLessonProgress(1, 1)).thenReturn(Optional.of(testProgress));

        // Act & Assert
        mockMvc.perform(get("/api/v1/progress/lesson")
                .param("studentId", "1")
                .param("lessonId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.progressId").value(1))
                .andExpect(jsonPath("$.studentId").value(1))
                .andExpect(jsonPath("$.lessonId").value(1))
                .andExpect(jsonPath("$.watchedSeconds").value(300));

        verify(progressService, times(1)).getLessonProgress(1, 1);
    }

    @Test
    @DisplayName("Should return 404 when lesson progress not found")
    @WithMockUser(username = "testuser", roles = "USER")
    void testGetLessonProgressNotFound() throws Exception {
        // Arrange
        when(progressService.getLessonProgress(1, 999)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/progress/lesson")
                .param("studentId", "1")
                .param("lessonId", "999"))
                .andExpect(status().isNotFound());

        verify(progressService, times(1)).getLessonProgress(1, 999);
    }

    // ============================================
    // Get All Progress Endpoint Tests
    // ============================================

    @Test
    @DisplayName("Should get all progress records for student")
    @WithMockUser(username = "testuser", roles = "USER")
    void testGetAllProgressByStudent() throws Exception {
        // Arrange
        List<Progress> progressList = new ArrayList<>();
        progressList.add(testProgress);
        progressList.add(testProgress.builder().progressId(2).lessonId(2).watchedSeconds(500).build());

        when(progressService.getAllProgressByStudent(1)).thenReturn(progressList);

        // Act & Assert
        mockMvc.perform(get("/api/v1/progress/all/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].studentId").value(1))
                .andExpect(jsonPath("$[1].lessonId").value(2));

        verify(progressService, times(1)).getAllProgressByStudent(1);
    }

    @Test
    @DisplayName("Should return empty list when student has no progress")
    @WithMockUser(username = "testuser", roles = "USER")
    void testGetAllProgressByStudentEmpty() throws Exception {
        // Arrange
        when(progressService.getAllProgressByStudent(999)).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/api/v1/progress/all/999"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(progressService, times(1)).getAllProgressByStudent(999);
    }

    // ============================================
    // Issue Certificate Endpoint Tests
    // ============================================

    @Test
    @DisplayName("Should issue certificate successfully")
    @WithMockUser(username = "testuser", roles = "USER")
    void testIssueCertificate() throws Exception {
        // Arrange
        when(progressService.issueCertificate(1, 1)).thenReturn(testCertificate);

        // Act & Assert
        mockMvc.perform(post("/api/v1/progress/certificates/issue")
                .param("studentId", "1")
                .param("courseId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.certificateId").value(1))
                .andExpect(jsonPath("$.studentId").value(1))
                .andExpect(jsonPath("$.courseId").value(1))
                .andExpect(jsonPath("$.verificationCode").value("EL-2026-EL-ABC123"));

        verify(progressService, times(1)).issueCertificate(1, 1);
    }

    @Test
    @DisplayName("Should handle multiple certificate issuances")
    @WithMockUser(username = "testuser", roles = "USER")
    void testIssueCertificateMultiple() throws Exception {
        // Arrange
        when(progressService.issueCertificate(anyInt(), anyInt()))
                .thenAnswer(invocation -> {
                    Integer studentId = invocation.getArgument(0);
                    Integer courseId = invocation.getArgument(1);
                    return testCertificate.builder()
                            .studentId(studentId)
                            .courseId(courseId)
                            .certificateId(studentId * 100 + courseId)
                            .build();
                });

        // Act & Assert
        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/api/v1/progress/certificates/issue")
                    .param("studentId", String.valueOf(i))
                    .param("courseId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.studentId").value(i));
        }

        verify(progressService, times(3)).issueCertificate(anyInt(), anyInt());
    }

    // ============================================
    // Get Student Certificates Endpoint Tests
    // ============================================

    @Test
    @DisplayName("Should get all certificates for student")
    @WithMockUser(username = "testuser", roles = "USER")
    void testGetStudentCertificates() throws Exception {
        // Arrange
        List<Certificate> certificateList = new ArrayList<>();
        certificateList.add(testCertificate);
        certificateList.add(testCertificate.builder()
                .certificateId(2)
                .courseId(2)
                .verificationCode("EL-2026-EL-DEF456")
                .build());

        when(progressService.getAllCertificatesByStudent(1)).thenReturn(certificateList);

        // Act & Assert
        mockMvc.perform(get("/api/v1/progress/certificates/student/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].studentId").value(1))
                .andExpect(jsonPath("$[1].courseId").value(2));

        verify(progressService, times(1)).getAllCertificatesByStudent(1);
    }

    @Test
    @DisplayName("Should return empty list when student has no certificates")
    @WithMockUser(username = "testuser", roles = "USER")
    void testGetStudentCertificatesEmpty() throws Exception {
        // Arrange
        when(progressService.getAllCertificatesByStudent(999)).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/api/v1/progress/certificates/student/999"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(progressService, times(1)).getAllCertificatesByStudent(999);
    }

    // ============================================
    // Verify Certificate Endpoint Tests
    // ============================================

    @Test
    @DisplayName("Should verify certificate by verification code")
    void testVerifyCertificate() throws Exception {
        // Arrange
        String verificationCode = "EL-2026-EL-ABC123";
        when(progressService.verifyCertificate(verificationCode)).thenReturn(Optional.of(testCertificate));

        // Act & Assert
        mockMvc.perform(get("/api/v1/progress/certificates/verify/{verificationCode}", verificationCode))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.certificateId").value(1))
                .andExpect(jsonPath("$.verificationCode").value(verificationCode))
                .andExpect(jsonPath("$.studentId").value(1));

        verify(progressService, times(1)).verifyCertificate(verificationCode);
    }

    @Test
    @DisplayName("Should return 404 when certificate verification code not found")
    void testVerifyCertificateNotFound() throws Exception {
        // Arrange
        String invalidCode = "INVALID-CODE";
        when(progressService.verifyCertificate(invalidCode)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/progress/certificates/verify/{verificationCode}", invalidCode))
                .andExpect(status().isNotFound());

        verify(progressService, times(1)).verifyCertificate(invalidCode);
    }

    // ============================================
    // Integration Tests
    // ============================================

    @Test
    @DisplayName("Should handle complete workflow: track progress -> get progress -> issue certificate")
    @WithMockUser(username = "testuser", roles = "USER")
    void testCompleteWorkflow() throws Exception {
        // Arrange
        doNothing().when(progressService).trackProgress(anyInt(), anyInt(), anyInt(), anyInt());
        when(progressService.getCourseProgress(1, 1)).thenReturn(100);
        when(progressService.issueCertificate(1, 1)).thenReturn(testCertificate);

        // Act & Assert - Step 1: Track progress
        Map<String, Integer> trackRequest = new HashMap<>();
        trackRequest.put("studentId", 1);
        trackRequest.put("courseId", 1);
        trackRequest.put("lessonId", 1);
        trackRequest.put("watchedSeconds", 300);

        mockMvc.perform(put("/api/v1/progress/track")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trackRequest)))
                .andExpect(status().isOk());

        // Step 2: Get course progress
        mockMvc.perform(get("/api/v1/progress/course")
                .param("studentId", "1")
                .param("courseId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json("100"));

        // Step 3: Issue certificate
        mockMvc.perform(post("/api/v1/progress/certificates/issue")
                .param("studentId", "1")
                .param("courseId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.certificateId").value(1))
                .andExpect(jsonPath("$.verificationCode").exists());

        verify(progressService, times(1)).trackProgress(anyInt(), anyInt(), anyInt(), anyInt());
        verify(progressService, times(1)).getCourseProgress(1, 1);
        verify(progressService, times(1)).issueCertificate(1, 1);
    }
}

