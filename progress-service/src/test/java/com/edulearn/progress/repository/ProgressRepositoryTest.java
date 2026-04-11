package com.edulearn.progress.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.edulearn.progress.entity.Progress;

/**
 * Integration tests for ProgressRepository
 * Tests database operations for Progress entity
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ProgressRepository Test Suite")
class ProgressRepositoryTest {

    @Autowired
    private ProgressRepository progressRepository;

    private Progress testProgress1;
    private Progress testProgress2;
    private Progress testProgress3;

    @BeforeEach
    void setUp() {
        progressRepository.deleteAll();

        // Test data setup
        testProgress1 = Progress.builder()
                .studentId(1)
                .courseId(1)
                .lessonId(1)
                .watchedSeconds(300)
                .isCompleted(false)
                .lastAccessedAt(LocalDateTime.now())
                .build();

        testProgress2 = Progress.builder()
                .studentId(1)
                .courseId(1)
                .lessonId(2)
                .watchedSeconds(500)
                .isCompleted(true)
                .lastAccessedAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();

        testProgress3 = Progress.builder()
                .studentId(1)
                .courseId(2)
                .lessonId(1)
                .watchedSeconds(100)
                .isCompleted(false)
                .lastAccessedAt(LocalDateTime.now())
                .build();
    }

    // ============================================
    // Save and Retrieve Tests
    // ============================================

    @Test
    @DisplayName("Should save progress record successfully")
    void testSaveProgress() {
        // Act
        Progress savedProgress = progressRepository.save(testProgress1);

        // Assert
        assertNotNull(savedProgress.getProgressId());
        assertEquals(1, savedProgress.getStudentId());
        assertEquals(1, savedProgress.getCourseId());
        assertEquals(1, savedProgress.getLessonId());
        assertEquals(300, savedProgress.getWatchedSeconds());
        assertFalse(savedProgress.getIsCompleted());
    }

    @Test
    @DisplayName("Should retrieve progress record by ID")
    void testFindProgressById() {
        // Arrange
        Progress savedProgress = progressRepository.save(testProgress1);

        // Act
        Optional<Progress> retrievedProgress = progressRepository.findById(savedProgress.getProgressId());

        // Assert
        assertTrue(retrievedProgress.isPresent());
        assertEquals(savedProgress.getProgressId(), retrievedProgress.get().getProgressId());
        assertEquals(savedProgress.getStudentId(), retrievedProgress.get().getStudentId());
    }

    // ============================================
    // Find By StudentId and LessonId Tests
    // ============================================

    @Test
    @DisplayName("Should find progress by student ID and lesson ID")
    void testFindByStudentIdAndLessonId() {
        // Arrange
        progressRepository.save(testProgress1);

        // Act
        Optional<Progress> result = progressRepository.findByStudentIdAndLessonId(1, 1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getStudentId());
        assertEquals(1, result.get().getLessonId());
    }

    @Test
    @DisplayName("Should return empty when progress not found by student ID and lesson ID")
    void testFindByStudentIdAndLessonIdNotFound() {
        // Arrange
        progressRepository.save(testProgress1);

        // Act
        Optional<Progress> result = progressRepository.findByStudentIdAndLessonId(999, 999);

        // Assert
        assertFalse(result.isPresent());
    }

    // ============================================
    // Find By StudentId and CourseId Tests
    // ============================================

    @Test
    @DisplayName("Should find multiple progress records by student ID and course ID")
    void testFindByStudentIdAndCourseId() {
        // Arrange
        progressRepository.save(testProgress1);
        progressRepository.save(testProgress2);
        progressRepository.save(testProgress3);

        // Act
        List<Progress> results = progressRepository.findByStudentIdAndCourseId(1, 1);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(p -> p.getStudentId() == 1 && p.getCourseId() == 1));
    }

    @Test
    @DisplayName("Should return empty list when no progress found by student ID and course ID")
    void testFindByStudentIdAndCourseIdEmpty() {
        // Arrange
        progressRepository.save(testProgress1);

        // Act
        List<Progress> results = progressRepository.findByStudentIdAndCourseId(999, 999);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    // ============================================
    // Count Completed Lessons Tests
    // ============================================

    @Test
    @DisplayName("Should count completed lessons correctly")
    void testCountByStudentIdAndCourseIdAndIsCompleted() {
        // Arrange
        progressRepository.save(testProgress1); // Not completed
        progressRepository.save(testProgress2); // Completed
        progressRepository.save(testProgress3); // Not completed

        // Act
        int completedCount = progressRepository.countByStudentIdAndCourseIdAndIsCompleted(1, 1, true);
        int notCompletedCount = progressRepository.countByStudentIdAndCourseIdAndIsCompleted(1, 1, false);

        // Assert
        assertEquals(1, completedCount);
        assertEquals(1, notCompletedCount);
    }

    @Test
    @DisplayName("Should return 0 when no completed lessons")
    void testCountCompletedLessonsZero() {
        // Arrange
        progressRepository.save(testProgress1); // Not completed
        progressRepository.save(testProgress3); // Not completed

        // Act
        int completedCount = progressRepository.countByStudentIdAndCourseIdAndIsCompleted(1, 1, true);

        // Assert
        assertEquals(0, completedCount);
    }

    // ============================================
    // Find By StudentId Tests
    // ============================================

    @Test
    @DisplayName("Should find all progress records for a student")
    void testFindByStudentId() {
        // Arrange
        progressRepository.save(testProgress1);
        progressRepository.save(testProgress2);
        progressRepository.save(testProgress3);

        // Act
        List<Progress> results = progressRepository.findByStudentId(1);

        // Assert
        assertNotNull(results);
        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(p -> p.getStudentId() == 1));
    }

    @Test
    @DisplayName("Should return empty list when student has no progress")
    void testFindByStudentIdEmpty() {
        // Arrange
        progressRepository.save(testProgress1);

        // Act
        List<Progress> results = progressRepository.findByStudentId(999);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should return only specific student's progress when multiple students exist")
    void testFindByStudentIdMultipleStudents() {
        // Arrange
        progressRepository.save(testProgress1);
        progressRepository.save(testProgress2);
        progressRepository.save(testProgress3);

        Progress studentProgress4 = Progress.builder()
                .studentId(2)
                .courseId(1)
                .lessonId(1)
                .watchedSeconds(200)
                .isCompleted(false)
                .build();
        progressRepository.save(studentProgress4);

        // Act
        List<Progress> student1Results = progressRepository.findByStudentId(1);
        List<Progress> student2Results = progressRepository.findByStudentId(2);

        // Assert
        assertEquals(3, student1Results.size());
        assertEquals(1, student2Results.size());
    }

    // ============================================
    // Update Tests
    // ============================================

    @Test
    @DisplayName("Should update progress record")
    void testUpdateProgress() {
        // Arrange
        Progress savedProgress = progressRepository.save(testProgress1);

        // Act
        savedProgress.setWatchedSeconds(600);
        savedProgress.setIsCompleted(true);
        savedProgress.setCompletedAt(LocalDateTime.now());
        Progress updatedProgress = progressRepository.save(savedProgress);

        // Assert
        Optional<Progress> retrievedProgress = progressRepository.findById(updatedProgress.getProgressId());
        assertTrue(retrievedProgress.isPresent());
        assertEquals(600, retrievedProgress.get().getWatchedSeconds());
        assertTrue(retrievedProgress.get().getIsCompleted());
        assertNotNull(retrievedProgress.get().getCompletedAt());
    }

    // ============================================
    // Delete Tests
    // ============================================

    @Test
    @DisplayName("Should delete progress record by ID")
    void testDeleteProgress() {
        // Arrange
        Progress savedProgress = progressRepository.save(testProgress1);
        Integer progressId = savedProgress.getProgressId();

        // Act
        progressRepository.deleteById(progressId);

        // Assert
        Optional<Progress> retrievedProgress = progressRepository.findById(progressId);
        assertFalse(retrievedProgress.isPresent());
    }

    @Test
    @DisplayName("Should delete all progress records")
    void testDeleteAllProgress() {
        // Arrange
        progressRepository.save(testProgress1);
        progressRepository.save(testProgress2);
        progressRepository.save(testProgress3);

        // Act
        progressRepository.deleteAll();

        // Assert
        assertEquals(0, progressRepository.count());
    }

    // ============================================
    // Multiple Records Tests
    // ============================================

    @Test
    @DisplayName("Should handle multiple progress records for same student and course")
    void testMultipleProgressSameCourse() {
        // Arrange
        Progress progress1 = Progress.builder()
                .studentId(1)
                .courseId(1)
                .lessonId(1)
                .watchedSeconds(300)
                .isCompleted(false)
                .build();

        Progress progress2 = Progress.builder()
                .studentId(1)
                .courseId(1)
                .lessonId(2)
                .watchedSeconds(500)
                .isCompleted(true)
                .build();

        Progress progress3 = Progress.builder()
                .studentId(1)
                .courseId(1)
                .lessonId(3)
                .watchedSeconds(400)
                .isCompleted(false)
                .build();

        progressRepository.save(progress1);
        progressRepository.save(progress2);
        progressRepository.save(progress3);

        // Act
        List<Progress> courseProgress = progressRepository.findByStudentIdAndCourseId(1, 1);
        int completedCount = progressRepository.countByStudentIdAndCourseIdAndIsCompleted(1, 1, true);

        // Assert
        assertEquals(3, courseProgress.size());
        assertEquals(1, completedCount);
    }
}

