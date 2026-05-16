package com.edulearn.progress.controller;

import com.edulearn.progress.entity.Certificate;
import com.edulearn.progress.entity.Progress;
import com.edulearn.progress.service.ProgressService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Progress Controller Tests")
class ProgressControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProgressService progressService;

    @InjectMocks
    private ProgressController progressController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(progressController)
                .build();
    }

    @Test
    @DisplayName("GET /course - Success")
    void testGetProgress() throws Exception {

        when(progressService.getCourseProgress(101L, 5L)).thenReturn(75);

        mockMvc.perform(get("/api/v1/progress/course")
                        .param("studentId", "101")
                        .param("courseId", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string("75"));
    }

    @Test
    @DisplayName("GET /all/{studentId} - Success")
    void testGetStudentProgress() throws Exception {

        Progress p = new Progress();
        p.setStudentId(101L);

        when(progressService.getAllProgressByStudent(101L))
                .thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/progress/all/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentId").value(101L));
    }

    @Test
    @DisplayName("PUT /track - Success")
    void testTrackProgress() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("studentId", 1L);
        request.put("courseId", 1L);
        request.put("lessonId", 1L);
        request.put("watchedSeconds", 300);

        doNothing().when(progressService).trackProgress(1L, 1L, 1L, 300);

        mockMvc.perform(put("/api/v1/progress/track")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /complete - Success")
    void testMarkLessonComplete() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("studentId", 1L);
        request.put("courseId", 1L);
        request.put("lessonId", 1L);

        Map<String, Object> result = Map.of("success", true);
        when(progressService.markLessonComplete(1L, 1L, 1L)).thenReturn(result);

        mockMvc.perform(put("/api/v1/progress/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /lesson - Success")
    void testGetLessonProgressSuccess() throws Exception {
        Progress p = new Progress();
        p.setStudentId(1L);
        p.setLessonId(1L);

        when(progressService.getLessonProgress(1L, 1L)).thenReturn(Optional.of(p));

        mockMvc.perform(get("/api/v1/progress/lesson")
                .param("studentId", "1")
                .param("lessonId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(1L));
    }

    @Test
    @DisplayName("GET /lesson - Not Found")
    void testGetLessonProgressNotFound() throws Exception {
        when(progressService.getLessonProgress(1L, 1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/progress/lesson")
                .param("studentId", "1")
                .param("lessonId", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /certificates/issue - Success")
    void testIssueCertificate() throws Exception {
        Certificate cert = new Certificate();
        cert.setCertificateId(1L);

        when(progressService.issueCertificate(1L, 1L)).thenReturn(cert);

        mockMvc.perform(post("/api/v1/progress/certificates/issue")
                .param("studentId", "1")
                .param("courseId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.certificateId").value(1L));
    }

    @Test
    @DisplayName("GET /certificates/student/{studentId} - Success")
    void testGetStudentCertificates() throws Exception {
        Certificate cert = new Certificate();
        cert.setStudentId(1L);

        when(progressService.getAllCertificatesByStudent(1L)).thenReturn(List.of(cert));

        mockMvc.perform(get("/api/v1/progress/certificates/student/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].studentId").value(1L));
    }

    @Test
    @DisplayName("GET /certificates/verify/{code} - Success")
    void testVerifyCertificateSuccess() throws Exception {
        Certificate cert = new Certificate();
        cert.setVerificationCode("EL-123");

        when(progressService.verifyCertificate("EL-123")).thenReturn(Optional.of(cert));

        mockMvc.perform(get("/api/v1/progress/certificates/verify/EL-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationCode").value("EL-123"));
    }

    @Test
    @DisplayName("GET /certificates/verify/{code} - Not Found")
    void testVerifyCertificateNotFound() throws Exception {
        when(progressService.verifyCertificate("INVALID")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/progress/certificates/verify/INVALID"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /certificates/download/{filename} - Not Found")
    void testDownloadCertificateNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/progress/certificates/download/missing.pdf"))
                .andExpect(status().isNotFound());
    }
}