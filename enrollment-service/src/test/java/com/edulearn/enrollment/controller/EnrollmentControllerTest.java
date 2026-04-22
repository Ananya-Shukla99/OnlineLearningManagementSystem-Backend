package com.edulearn.enrollment.controller;

import com.edulearn.enrollment.entity.Enrollment;
import com.edulearn.enrollment.service.EnrollmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.context.TestPropertySource;

@WebMvcTest(EnrollmentController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
    "spring.cloud.discovery.enabled=false",
    "spring.cloud.config.enabled=false",
    "eureka.client.enabled=false"
})
@DisplayName("Enrollment Controller Tests")
class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnrollmentService enrollmentService;

    @MockBean
    private com.edulearn.enrollment.config.JwtAuthFilter jwtAuthFilter;

    @MockBean
    private com.edulearn.enrollment.config.JwtUtil jwtUtil;

    @MockBean
    private org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory;

    @Autowired
    private ObjectMapper objectMapper;

    private Enrollment testEnrollment;

    @BeforeEach
    void setUp() {
        testEnrollment = Enrollment.builder()
                .enrollmentId(1L)
                .studentId(10L)
                .courseId(5L)
                .enrolledAt(LocalDateTime.now())
                .status("ACTIVE")
                .progressPercent(0)
                .certificateIssued(false)
                .build();
    }

    @Test
    @DisplayName("POST /enroll - Success")
    void testEnrollSuccess() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("studentId", 10L);
        request.put("courseId", 5L);

        when(enrollmentService.enroll(10L, 5L)).thenReturn(testEnrollment);

        mockMvc.perform(post("/api/v1/enrollments/enroll")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Successfully enrolled in course"));
    }

    @Test
    @DisplayName("POST /enroll - Missing studentId")
    void testEnrollMissingStudentId() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("courseId", 5L);

        mockMvc.perform(post("/api/v1/enrollments/enroll")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("studentId is required"));
    }

    @Test
    @DisplayName("POST /enroll - Missing courseId")
    void testEnrollMissingCourseId() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("studentId", 10L);

        mockMvc.perform(post("/api/v1/enrollments/enroll")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("courseId is required"));
    }

    @Test
    @DisplayName("DELETE /{enrollmentId} - Success")
    void testUnenrollSuccess() throws Exception {
        doNothing().when(enrollmentService).unenroll(1L);

        mockMvc.perform(delete("/api/v1/enrollments/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Successfully unenrolled from course"));
    }

    @Test
    @DisplayName("GET /student/{studentId} - Success")
    void testGetByStudent() throws Exception {
        when(enrollmentService.getEnrollmentsByStudent(10L)).thenReturn(List.of(testEnrollment));

        mockMvc.perform(get("/api/v1/enrollments/student/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].studentId").value(10L));
    }

    @Test
    @DisplayName("GET /course/{courseId} - Success")
    void testGetByCourse() throws Exception {
        when(enrollmentService.getEnrollmentsByCourse(5L)).thenReturn(List.of(testEnrollment));

        mockMvc.perform(get("/api/v1/enrollments/course/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].courseId").value(5L));
    }

    @Test
    @DisplayName("PUT /progress - Success")
    void testUpdateProgress() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("studentId", 10L);
        request.put("courseId", 5L);
        request.put("progressPercent", 50);

        doNothing().when(enrollmentService).updateProgress(10L, 5L, 50);

        mockMvc.perform(put("/api/v1/enrollments/progress")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Progress updated successfully"));
    }

    @Test
    @DisplayName("PUT /complete/{enrollmentId} - Success")
    void testMarkComplete() throws Exception {
        doNothing().when(enrollmentService).markComplete(1L);

        mockMvc.perform(put("/api/v1/enrollments/complete/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Enrollment marked as completed"));
    }

    @Test
    @DisplayName("GET /check - Success")
    void testIsEnrolled() throws Exception {
        when(enrollmentService.isEnrolled(10L, 5L)).thenReturn(true);

        mockMvc.perform(get("/api/v1/enrollments/check?studentId=10&courseId=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("GET /count/{courseId} - Success")
    void testGetCount() throws Exception {
        when(enrollmentService.getEnrollmentCount(5L)).thenReturn(100);

        mockMvc.perform(get("/api/v1/enrollments/count/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(100));
    }
}
