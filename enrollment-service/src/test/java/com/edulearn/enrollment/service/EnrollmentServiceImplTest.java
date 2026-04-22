package com.edulearn.enrollment.service;

import com.edulearn.enrollment.client.CourseClient;
import com.edulearn.enrollment.entity.Enrollment;
import com.edulearn.enrollment.repository.EnrollmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Enrollment Service Tests")
class EnrollmentServiceImplTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private CourseClient courseClient;

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    private Enrollment testEnrollment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

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

    // ==================== ENROLL TESTS ====================

    @Test
    @DisplayName("Should enroll student successfully when not already enrolled")
    void testEnrollSuccess() {
        // Arrange
        when(enrollmentRepository.existsByStudentIdAndCourseId(10L, 5L)).thenReturn(false);
        
        Map<String, Object> courseResponse = new HashMap<>();
        courseResponse.put("data", Map.of("isPublished", true));
        when(courseClient.getCourse(5L)).thenReturn(courseResponse);
        
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);

        // Act
        Enrollment result = enrollmentService.enroll(10L, 5L);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getStudentId());
        assertEquals(5L, result.getCourseId());
        assertEquals("ACTIVE", result.getStatus());
        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), (Object) any());
    }

    @Test
    @DisplayName("Should throw exception when course is unpublished")
    void testEnrollUnpublishedCourse() {
        // Arrange
        when(enrollmentRepository.existsByStudentIdAndCourseId(10L, 5L)).thenReturn(false);
        
        Map<String, Object> courseResponse = new HashMap<>();
        courseResponse.put("data", Map.of("isPublished", false));
        when(courseClient.getCourse(5L)).thenReturn(courseResponse);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> enrollmentService.enroll(10L, 5L));
        assertEquals("Cannot enroll in an unpublished course", exception.getMessage());
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("Should return false when Feign call fails")
    void testEnrollFeignFailure() {
        // Arrange
        when(enrollmentRepository.existsByStudentIdAndCourseId(10L, 5L)).thenReturn(false);
        when(courseClient.getCourse(5L)).thenThrow(new RuntimeException("Feign error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> enrollmentService.enroll(10L, 5L));
        assertEquals("Cannot enroll in an unpublished course", exception.getMessage());
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("Should succeed even if RabbitMQ notification fails")
    void testEnrollRabbitFailure() {
        // Arrange
        when(enrollmentRepository.existsByStudentIdAndCourseId(10L, 5L)).thenReturn(false);
        Map<String, Object> courseResponse = new HashMap<>();
        courseResponse.put("data", Map.of("isPublished", true));
        when(courseClient.getCourse(5L)).thenReturn(courseResponse);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);
        
        // Mock rabbit template to throw exception
        doThrow(new RuntimeException("Rabbit error")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        // Act
        Enrollment result = enrollmentService.enroll(10L, 5L);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getStudentId());
        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
        // Verify it didn't crash
    }

    @Test
    @DisplayName("Should throw exception when student already enrolled")
    void testEnrollAlreadyEnrolled() {
        // Arrange
        when(enrollmentRepository.existsByStudentIdAndCourseId(10L, 5L)).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            enrollmentService.enroll(10L, 5L);
        });
        assertEquals("Student already enrolled in this course", exception.getMessage());
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    // ==================== UNENROLL TESTS ====================

    @Test
    @DisplayName("Should unenroll student successfully")
    void testUnenrollSuccess() {
        // Arrange
        testEnrollment.setStatus("ACTIVE");
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(testEnrollment));
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);

        // Act
        enrollmentService.unenroll(1L);

        // Assert
        assertEquals("CANCELLED", testEnrollment.getStatus());
        verify(enrollmentRepository, times(1)).save(testEnrollment);
    }

    @Test
    @DisplayName("Should throw exception when unenrolling non-existent enrollment")
    void testUnenrollNotFound() {
        // Arrange
        when(enrollmentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            enrollmentService.unenroll(999L);
        });
        assertEquals("Enrollment not found with ID: 999", exception.getMessage());
    }

    // ==================== GET ENROLLMENTS TESTS ====================

    @Test
    @DisplayName("Should get all enrollments by student")
    void testGetEnrollmentsByStudent() {
        // Arrange
        List<Enrollment> enrollments = List.of(testEnrollment);
        when(enrollmentRepository.findByStudentId(10L)).thenReturn(enrollments);

        // Act
        List<Enrollment> result = enrollmentService.getEnrollmentsByStudent(10L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getStudentId());
        verify(enrollmentRepository, times(1)).findByStudentId(10L);
    }

    @Test
    @DisplayName("Should get all enrollments by course")
    void testGetEnrollmentsByCourse() {
        // Arrange
        List<Enrollment> enrollments = List.of(testEnrollment);
        when(enrollmentRepository.findByCourseId(5L)).thenReturn(enrollments);

        // Act
        List<Enrollment> result = enrollmentService.getEnrollmentsByCourse(5L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getCourseId());
        verify(enrollmentRepository, times(1)).findByCourseId(5L);
    }

    // ==================== PROGRESS UPDATE TESTS ====================

    @Test
    @DisplayName("Should update progress successfully")
    void testUpdateProgressSuccess() {
        // Arrange
        testEnrollment.setProgressPercent(0);
        when(enrollmentRepository.findByStudentIdAndCourseId(10L, 5L)).thenReturn(Optional.of(testEnrollment));
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);

        // Act
        enrollmentService.updateProgress(10L, 5L, 50);

        // Assert
        assertEquals(50, testEnrollment.getProgressPercent());
        assertEquals("ACTIVE", testEnrollment.getStatus());
        verify(enrollmentRepository, times(1)).save(testEnrollment);
    }

    @Test
    @DisplayName("Should auto-complete when progress reaches 100%")
    void testUpdateProgressAutoComplete() {
        // Arrange
        testEnrollment.setProgressPercent(0);
        testEnrollment.setStatus("ACTIVE");
        when(enrollmentRepository.findByStudentIdAndCourseId(10L, 5L)).thenReturn(Optional.of(testEnrollment));
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);

        // Act
        enrollmentService.updateProgress(10L, 5L, 100);

        // Assert
        assertEquals(100, testEnrollment.getProgressPercent());
        assertEquals("COMPLETED", testEnrollment.getStatus());
        assertNotNull(testEnrollment.getCompletedAt());
        verify(enrollmentRepository, times(1)).save(testEnrollment);
    }

    @Test
    @DisplayName("Should throw exception when updating progress for non-existent enrollment")
    void testUpdateProgressNotFound() {
        // Arrange
        when(enrollmentRepository.findByStudentIdAndCourseId(10L, 5L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            enrollmentService.updateProgress(10L, 5L, 50);
        });
        assertEquals("Enrollment not found", exception.getMessage());
    }

    // ==================== MARK COMPLETE TESTS ====================

    @Test
    @DisplayName("Should mark enrollment as completed")
    void testMarkCompleteSuccess() {
        // Arrange
        testEnrollment.setStatus("ACTIVE");
        testEnrollment.setProgressPercent(50);
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(testEnrollment));
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);

        // Act
        enrollmentService.markComplete(1L);

        // Assert
        assertEquals("COMPLETED", testEnrollment.getStatus());
        assertEquals(100, testEnrollment.getProgressPercent());
        assertNotNull(testEnrollment.getCompletedAt());
        verify(enrollmentRepository, times(1)).save(testEnrollment);
    }

    // ==================== IS ENROLLED TESTS (CRITICAL) ====================

    @Test
    @DisplayName("Should return true when student is enrolled")
    void testIsEnrolledTrue() {
        // Arrange
        when(enrollmentRepository.existsByStudentIdAndCourseId(10L, 5L)).thenReturn(true);

        // Act
        boolean result = enrollmentService.isEnrolled(10L, 5L);

        // Assert
        assertTrue(result);
        verify(enrollmentRepository, times(1)).existsByStudentIdAndCourseId(10L, 5L);
    }

    @Test
    @DisplayName("Should return false when student is not enrolled")
    void testIsEnrolledFalse() {
        // Arrange
        when(enrollmentRepository.existsByStudentIdAndCourseId(10L, 5L)).thenReturn(false);

        // Act
        boolean result = enrollmentService.isEnrolled(10L, 5L);

        // Assert
        assertFalse(result);
        verify(enrollmentRepository, times(1)).existsByStudentIdAndCourseId(10L, 5L);
    }

    // ==================== GET ENROLLMENT COUNT TESTS ====================

    @Test
    @DisplayName("Should get enrollment count for course")
    void testGetEnrollmentCount() {
        // Arrange
        when(enrollmentRepository.countByCourseId(5L)).thenReturn(42);

        // Act
        int result = enrollmentService.getEnrollmentCount(5L);

        // Assert
        assertEquals(42, result);
        verify(enrollmentRepository, times(1)).countByCourseId(5L);
    }

    @Test
    @DisplayName("Should return zero when no enrollments for course")
    void testGetEnrollmentCountZero() {
        // Arrange
        when(enrollmentRepository.countByCourseId(5L)).thenReturn(0);

        // Act
        int result = enrollmentService.getEnrollmentCount(5L);

        // Assert
        assertEquals(0, result);
        verify(enrollmentRepository, times(1)).countByCourseId(5L);
    }
}
