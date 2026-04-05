package com.edulearn.enrollment.controller;

import com.edulearn.enrollment.entity.Enrollment;
import com.edulearn.enrollment.service.EnrollmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Enrollment Controller Tests")
class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnrollmentService enrollmentService;

    @Autowired
    private ObjectMapper objectMapper;

    private Enrollment testEnrollment;

    @BeforeEach
    void setUp() {
        testEnrollment = Enrollment.builder()
                .enrollmentId(1L)
                .studentId(10L)
                .courseId(5)
                .enrolledAt(LocalDateTime.now())
                .status("ACTIVE")
                .progressPercent(0)
                .certificateIssued(false)
                .build();
    } 

    // ==================== POST /enrollments/enroll Tests ====================

    @Test
    @DisplayName("Should enroll as STUDENT role")
    @WithMockUser(username = "student@test.com", authorities = "STUDENT")
    void testEnrollAsStudent() throws Exception {
        // Arrange
        when(enrollmentService.enroll(10L, 5)).thenReturn(testEnrollment);

        // Act & Assert
        mockMvc.perform(post("/api/v1/enrollments/enroll")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        java.util.Map.of("studentId", 10L, "courseId", 5))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Successfully enrolled in course"))
                .andExpect(jsonPath("$.data.enrollmentId").value(1))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        verify(enrollmentService, times(1)).enroll(10L, 5);
    }

    @Test
    @DisplayName("Should reject enroll with INSTRUCTOR role")
    @WithMockUser(username = "instructor@test.com", authorities = "INSTRUCTOR")
    void testEnrollAsInstructor() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/enrollments/enroll")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        java.util.Map.of("studentId", 10L, "courseId", 5))))
                .andExpect(status().isForbidden());

        verify(enrollmentService, never()).enroll(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Should reject enroll without authentication")
    void testEnrollWithoutAuth() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/enrollments/enroll")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        java.util.Map.of("studentId", 10L, "courseId", 5))))
                .andExpect(status().isUnauthorized());

        verify(enrollmentService, never()).enroll(anyLong(), anyInt());
    }

    // ==================== DELETE /enrollments/{id} Tests ====================

    @Test
    @DisplayName("Should unenroll as STUDENT role")
    @WithMockUser(username = "student@test.com", authorities = "STUDENT")
    void testUnenrollAsStudent() throws Exception {
        // Arrange
        doNothing().when(enrollmentService).unenroll(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/enrollments/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Successfully unenrolled from course"));

        verify(enrollmentService, times(1)).unenroll(1L);
    }

    @Test
    @DisplayName("Should reject unenroll with INSTRUCTOR role")
    @WithMockUser(username = "instructor@test.com", authorities = "INSTRUCTOR")
    void testUnenrollAsInstructor() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/enrollments/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(enrollmentService, never()).unenroll(anyLong());
    }

    // ==================== GET /enrollments/student/{studentId} Tests ====================

    @Test
    @DisplayName("Should get student enrollments as authenticated user")
    @WithMockUser(username = "student@test.com", authorities = "STUDENT")
    void testGetStudentEnrollments() throws Exception {
        // Arrange
        List<Enrollment> enrollments = Arrays.asList(testEnrollment);
        when(enrollmentService.getEnrollmentsByStudent(10L)).thenReturn(enrollments);

        // Act & Assert
        mockMvc.perform(get("/api/v1/enrollments/student/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].studentId").value(10));

        verify(enrollmentService, times(1)).getEnrollmentsByStudent(10L);
    }

    @Test
    @DisplayName("Should reject get student enrollments without authentication")
    void testGetStudentEnrollmentsWithoutAuth() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/enrollments/student/10"))
                .andExpect(status().isUnauthorized());

        verify(enrollmentService, never()).getEnrollmentsByStudent(anyLong());
    }

    // ==================== GET /enrollments/course/{courseId} Tests ====================

    @Test
    @DisplayName("Should get course enrollments as INSTRUCTOR role")
    @WithMockUser(username = "instructor@test.com", authorities = "INSTRUCTOR")
    void testGetCourseEnrollmentsAsInstructor() throws Exception {
        // Arrange
        List<Enrollment> enrollments = Arrays.asList(testEnrollment);
        when(enrollmentService.getEnrollmentsByCourse(5)).thenReturn(enrollments);

        // Act & Assert
        mockMvc.perform(get("/api/v1/enrollments/course/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].courseId").value(5));

        verify(enrollmentService, times(1)).getEnrollmentsByCourse(5);
    }

    @Test
    @DisplayName("Should get course enrollments as ADMIN role")
    @WithMockUser(username = "admin@test.com", authorities = "ADMIN")
    void testGetCourseEnrollmentsAsAdmin() throws Exception {
        // Arrange
        List<Enrollment> enrollments = Arrays.asList(testEnrollment);
        when(enrollmentService.getEnrollmentsByCourse(5)).thenReturn(enrollments);

        // Act & Assert
        mockMvc.perform(get("/api/v1/enrollments/course/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(enrollmentService, times(1)).getEnrollmentsByCourse(5);
    }

    @Test
    @DisplayName("Should reject course enrollments with STUDENT role")
    @WithMockUser(username = "student@test.com", authorities = "STUDENT")
    void testGetCourseEnrollmentsAsStudent() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/enrollments/course/5"))
                .andExpect(status().isForbidden());

        verify(enrollmentService, never()).getEnrollmentsByCourse(anyInt());
    }

    // ==================== PUT /enrollments/progress Tests ====================

    @Test
    @DisplayName("Should update progress as authenticated user")
    @WithMockUser(username = "student@test.com", authorities = "STUDENT")
    void testUpdateProgress() throws Exception {
        // Arrange
        doNothing().when(enrollmentService).updateProgress(10L, 5, 50);

        // Act & Assert
        mockMvc.perform(put("/api/v1/enrollments/progress")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        java.util.Map.of("studentId", 10L, "courseId", 5, "progressPercent", 50))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Progress updated successfully"));

        verify(enrollmentService, times(1)).updateProgress(10L, 5, 50);
    }

    @Test
    @DisplayName("Should reject progress update without authentication")
    void testUpdateProgressWithoutAuth() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/v1/enrollments/progress")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        java.util.Map.of("studentId", 10L, "courseId", 5, "progressPercent", 50))))
                .andExpect(status().isUnauthorized());

        verify(enrollmentService, never()).updateProgress(anyLong(), anyInt(), anyInt());
    }

    // ==================== PUT /enrollments/complete/{id} Tests ====================

    @Test
    @DisplayName("Should mark complete as INSTRUCTOR role")
    @WithMockUser(username = "instructor@test.com", authorities = "INSTRUCTOR")
    void testMarkCompleteAsInstructor() throws Exception {
        // Arrange
        doNothing().when(enrollmentService).markComplete(1L);

        // Act & Assert
        mockMvc.perform(put("/api/v1/enrollments/complete/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Enrollment marked as completed"));

        verify(enrollmentService, times(1)).markComplete(1L);
    }

    @Test
    @DisplayName("Should mark complete as ADMIN role")
    @WithMockUser(username = "admin@test.com", authorities = "ADMIN")
    void testMarkCompleteAsAdmin() throws Exception {
        // Arrange
        doNothing().when(enrollmentService).markComplete(1L);

        // Act & Assert
        mockMvc.perform(put("/api/v1/enrollments/complete/1")
                .with(csrf()))
                .andExpect(status().isOk());

        verify(enrollmentService, times(1)).markComplete(1L);
    }

    @Test
    @DisplayName("Should reject mark complete with STUDENT role")
    @WithMockUser(username = "student@test.com", authorities = "STUDENT")
    void testMarkCompleteAsStudent() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/v1/enrollments/complete/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(enrollmentService, never()).markComplete(anyLong());
    }

    // ==================== GET /enrollments/check (CRITICAL) Tests ====================

    @Test
    @DisplayName("Should check enrollment as authenticated user - enrolled")
    @WithMockUser(username = "student@test.com", authorities = "STUDENT")
    void testCheckEnrollmentTrue() throws Exception {
        // Arrange
        when(enrollmentService.isEnrolled(10L, 5)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/v1/enrollments/check?studentId=10&courseId=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));

        verify(enrollmentService, times(1)).isEnrolled(10L, 5);
    }

    @Test
    @DisplayName("Should check enrollment as authenticated user - not enrolled")
    @WithMockUser(username = "student@test.com", authorities = "STUDENT")
    void testCheckEnrollmentFalse() throws Exception {
        // Arrange
        when(enrollmentService.isEnrolled(10L, 5)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/v1/enrollments/check?studentId=10&courseId=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(false));

        verify(enrollmentService, times(1)).isEnrolled(10L, 5);
    }

    @Test
    @DisplayName("Should reject check enrollment without authentication")
    void testCheckEnrollmentWithoutAuth() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/enrollments/check?studentId=10&courseId=5"))
                .andExpect(status().isUnauthorized());

        verify(enrollmentService, never()).isEnrolled(anyLong(), anyInt());
    }

    // ==================== GET /enrollments/count/{courseId} Tests ====================

    @Test
    @DisplayName("Should get enrollment count as authenticated user")
    @WithMockUser(username = "student@test.com", authorities = "STUDENT")
    void testGetEnrollmentCount() throws Exception {
        // Arrange
        when(enrollmentService.getEnrollmentCount(5)).thenReturn(42);

        // Act & Assert
        mockMvc.perform(get("/api/v1/enrollments/count/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(42));

        verify(enrollmentService, times(1)).getEnrollmentCount(5);
    }

    @Test
    @DisplayName("Should reject enrollment count without authentication")
    void testGetEnrollmentCountWithoutAuth() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/enrollments/count/5"))
                .andExpect(status().isUnauthorized());

        verify(enrollmentService, never()).getEnrollmentCount(anyInt());
    }
}

