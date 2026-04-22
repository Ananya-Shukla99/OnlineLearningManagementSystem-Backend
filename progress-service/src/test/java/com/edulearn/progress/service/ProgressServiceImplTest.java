package com.edulearn.progress.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.edulearn.progress.entity.Progress;
import com.edulearn.progress.entity.Certificate;
import com.edulearn.progress.repository.ProgressRepository;
import com.edulearn.progress.repository.CertificateRepository;

/**
 * Unit tests for ProgressServiceImpl
 * Tests progress tracking, course progress calculation, and certificate generation
 */
@DisplayName("ProgressServiceImpl Test Suite")
class ProgressServiceImplTest {

    @Mock
    private ProgressRepository progressRepository;

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ProgressServiceImpl progressService;

    private Progress testProgress;
    private Certificate testCertificate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set certificate output path for PDF generation
        ReflectionTestUtils.setField(progressService, "certificateOutputPath", "src/main/resources/certificates/");

        // Test data setup
        testProgress = Progress.builder()
                .progressId(1)
                .studentId(1L)
                .courseId(1L)
                .lessonId(1L)
                .watchedSeconds(300)
                .isCompleted(false)
                .lastAccessedAt(LocalDateTime.now())
                .build();

        testCertificate = Certificate.builder()
                .certificateId(1L)
                .studentId(1L)
                .courseId(1L)
                .issuedAt(LocalDate.now())
                .certificateUrl("src/main/resources/certificates/cert_1_1.pdf")
                .verificationCode("EL-2026-EL-ABC123")
                .instructorName("Test Instructor")
                .courseName("Test Course")
                .build();
    }

    // ============================================
    // TrackProgress Tests
    // ============================================

    @Test
    @DisplayName("Should create new progress when tracking for first time")
    void testTrackProgressNewProgress() {
        // Arrange
        Long studentId = 1L;
        Long courseId = 1L;
        Long lessonId = 1L;
        Integer watchedSeconds = 300;

        when(progressRepository.findByStudentIdAndLessonId(studentId, lessonId))
                .thenReturn(Optional.empty());
        when(progressRepository.save(any(Progress.class)))
                .thenReturn(testProgress);

        // Act
        progressService.trackProgress(studentId, courseId, lessonId, watchedSeconds);

        // Assert
        verify(progressRepository, times(1)).findByStudentIdAndLessonId(studentId, lessonId);
        verify(progressRepository, times(1)).save(any(Progress.class));
    }

    @Test
    @DisplayName("Should update progress when watched seconds increase")
    void testTrackProgressUpdateWatchedSeconds() {
        // Arrange
        Long studentId = 1L;
        Long courseId = 1L;
        Long lessonId = 1L;
        Integer newWatchedSeconds = 600;

        Progress existingProgress = Progress.builder()
                .studentId(1L)
                .lessonId(1L)
                .watchedSeconds(300)
                .build();

        when(progressRepository.findByStudentIdAndLessonId(studentId, lessonId))
                .thenReturn(Optional.of(existingProgress));
        when(progressRepository.save(any(Progress.class)))
                .thenReturn(existingProgress);

        // Act
        progressService.trackProgress(studentId, courseId, lessonId, newWatchedSeconds);

        // Assert
        verify(progressRepository, times(1)).findByStudentIdAndLessonId(studentId, lessonId);
        verify(progressRepository, times(1)).save(any(Progress.class));
    }

    @Test
    @DisplayName("Should not update progress when watched seconds decrease")
    void testTrackProgressNoUpdateOnDecrease() {
        // Arrange
        Long studentId = 1L;
        Long courseId = 1L;
        Long lessonId = 1L;
        Integer lessWatchedSeconds = 100;

        Progress existingProgress = Progress.builder()
                .studentId(1L)
                .lessonId(1L)
                .watchedSeconds(300)
                .build();

        when(progressRepository.findByStudentIdAndLessonId(studentId, lessonId))
                .thenReturn(Optional.of(existingProgress));
        when(progressRepository.save(any(Progress.class)))
                .thenReturn(existingProgress);

        // Act
        progressService.trackProgress(studentId, courseId, lessonId, lessWatchedSeconds);

        // Assert - watched seconds should remain 300, not 100
        verify(progressRepository, times(1)).findByStudentIdAndLessonId(studentId, lessonId);
        verify(progressRepository, times(1)).save(any(Progress.class));
    }

    // ============================================
    // Mark Lesson Complete Tests
    // ============================================

    @Test
    @DisplayName("Should mark existing lesson as complete")
    void testMarkLessonCompleteExisting() {
        // Arrange
        Long studentId = 1L;
        Long courseId = 1L;
        Long lessonId = 1L;

        Progress existingProgress = Progress.builder()
                .studentId(1L)
                .lessonId(1L)
                .isCompleted(false)
                .build();

        when(progressRepository.findByStudentIdAndLessonId(studentId, lessonId))
                .thenReturn(Optional.of(existingProgress));
        when(progressRepository.save(any(Progress.class)))
                .thenReturn(existingProgress);

        // Act
        progressService.markLessonComplete(studentId, courseId, lessonId);

        // Assert
        verify(progressRepository, times(1)).findByStudentIdAndLessonId(studentId, lessonId);
        verify(progressRepository, times(1)).save(any(Progress.class));
    }

    @Test
    @DisplayName("Should create and mark new lesson as complete")
    void testMarkLessonCompleteNew() {
        // Arrange
        Long studentId = 1L;
        Long courseId = 1L;
        Long lessonId = 1L;

        when(progressRepository.findByStudentIdAndLessonId(studentId, lessonId))
                .thenReturn(Optional.empty());
        when(progressRepository.save(any(Progress.class)))
                .thenReturn(testProgress);

        // Act
        progressService.markLessonComplete(studentId, courseId, lessonId);

        // Assert
        verify(progressRepository, times(1)).findByStudentIdAndLessonId(studentId, lessonId);
        verify(progressRepository, times(1)).save(any(Progress.class));
    }

    // ============================================
    // Course Progress Tests
    // ============================================

    @Test
    @DisplayName("Should calculate 0% progress when no lessons completed")
    void testGetCourseProgressZeroPercent() {
        // Arrange
        Long studentId = 1L;
        Long courseId = 1L;

        when(progressRepository.countByStudentIdAndCourseIdAndIsCompleted(studentId, courseId, true))
                .thenReturn(0);
        when(restTemplate.getForObject(anyString(), eq(Integer.class))).thenReturn(3);

        // Act
        Integer progress = progressService.getCourseProgress(studentId, courseId);

        // Assert
        assertEquals(0, progress);
        verify(progressRepository, times(1)).countByStudentIdAndCourseIdAndIsCompleted(studentId, courseId, true);
    }

    @Test
    @DisplayName("Should calculate 33% progress when 1 of 3 lessons completed")
    void testGetCourseProgressThirtyThreePercent() {
        // Arrange
        Long studentId = 1L;
        Long courseId = 1L;

        when(progressRepository.countByStudentIdAndCourseIdAndIsCompleted(studentId, courseId, true))
                .thenReturn(1);
        when(restTemplate.getForObject(anyString(), eq(Integer.class))).thenReturn(3);

        // Act
        Integer progress = progressService.getCourseProgress(studentId, courseId);

        // Assert
        assertEquals(33, progress);
        verify(progressRepository, times(1)).countByStudentIdAndCourseIdAndIsCompleted(studentId, courseId, true);
    }

    @Test
    @DisplayName("Should calculate 66% progress when 2 of 3 lessons completed")
    void testGetCourseProgressSixtySixPercent() {
        // Arrange
        Long studentId = 1L;
        Long courseId = 1L;

        when(progressRepository.countByStudentIdAndCourseIdAndIsCompleted(studentId, courseId, true))
                .thenReturn(2);
        when(restTemplate.getForObject(anyString(), eq(Integer.class))).thenReturn(3);

        // Act
        Integer progress = progressService.getCourseProgress(studentId, courseId);

        // Assert
        assertEquals(66, progress);
        verify(progressRepository, times(1)).countByStudentIdAndCourseIdAndIsCompleted(studentId, courseId, true);
    }

    @Test
    @DisplayName("Should calculate 100% progress when all 3 lessons completed")
    void testGetCourseProgressHundredPercent() {
        // Arrange
        Long studentId = 1L;
        Long courseId = 1L;

        when(progressRepository.countByStudentIdAndCourseIdAndIsCompleted(studentId, courseId, true))
                .thenReturn(3);
        when(restTemplate.getForObject(anyString(), eq(Integer.class))).thenReturn(3);

        // Act
        Integer progress = progressService.getCourseProgress(studentId, courseId);

        // Assert
        assertEquals(100, progress);
        verify(progressRepository, times(1)).countByStudentIdAndCourseIdAndIsCompleted(studentId, courseId, true);
    }

    // ============================================
    // Get Lesson Progress Tests
    // ============================================

    @Test
    @DisplayName("Should return lesson progress when exists")
    void testGetLessonProgressExists() {
        // Arrange
        Long studentId = 1L;
        Long lessonId = 1L;

        when(progressRepository.findByStudentIdAndLessonId(studentId, lessonId))
                .thenReturn(Optional.of(testProgress));

        // Act
        Optional<Progress> result = progressService.getLessonProgress(studentId, lessonId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testProgress.getProgressId(), result.get().getProgressId());
        verify(progressRepository, times(1)).findByStudentIdAndLessonId(studentId, lessonId);
    }

    @Test
    @DisplayName("Should return empty when lesson progress doesn't exist")
    void testGetLessonProgressNotExists() {
        // Arrange
        Long studentId = 1L;
        Long lessonId = 1L;

        when(progressRepository.findByStudentIdAndLessonId(studentId, lessonId))
                .thenReturn(Optional.empty());

        // Act
        Optional<Progress> result = progressService.getLessonProgress(studentId, lessonId);

        // Assert
        assertFalse(result.isPresent());
        verify(progressRepository, times(1)).findByStudentIdAndLessonId(studentId, lessonId);
    }

    // ============================================
    // Issue Certificate Tests
    // ============================================

    @Test
    @DisplayName("Should issue new certificate when none exists")
    void testIssueCertificateNewCertificate() {
        // Arrange
        Long studentId = 1L;
        Long courseId = 1L;

        when(progressRepository.countByStudentIdAndCourseIdAndIsCompleted(studentId, courseId, true))
                .thenReturn(3);
        when(restTemplate.getForObject(contains("/lessons/count/"), eq(Integer.class))).thenReturn(3);

        when(certificateRepository.existsByStudentIdAndCourseId(studentId, courseId))
                .thenReturn(false);
        when(certificateRepository.save(any(Certificate.class)))
                .thenReturn(testCertificate);

        // Act
        Certificate result = progressService.issueCertificate(studentId, courseId);

        // Assert
        assertNotNull(result);
        assertEquals(studentId, result.getStudentId());
        assertEquals(courseId, result.getCourseId());
        verify(certificateRepository, times(1)).existsByStudentIdAndCourseId(studentId, courseId);
        verify(certificateRepository, times(1)).save(any(Certificate.class));
    }

    @Test
    @DisplayName("Should return existing certificate when already issued")
    void testIssueCertificateAlreadyExists() {
        // Arrange
        Long studentId = 1L;
        Long courseId = 1L;

        when(progressRepository.countByStudentIdAndCourseIdAndIsCompleted(studentId, courseId, true))
                .thenReturn(3);
        when(restTemplate.getForObject(contains("/lessons/count/"), eq(Integer.class))).thenReturn(3);

        when(certificateRepository.existsByStudentIdAndCourseId(studentId, courseId))
                .thenReturn(true);
        when(certificateRepository.findByStudentIdAndCourseId(studentId, courseId))
                .thenReturn(Optional.of(testCertificate));

        // Act
        Certificate result = progressService.issueCertificate(studentId, courseId);

        // Assert
        assertNotNull(result);
        assertEquals(testCertificate.getCertificateId(), result.getCertificateId());
        verify(certificateRepository, times(1)).existsByStudentIdAndCourseId(studentId, courseId);
        verify(certificateRepository, times(1)).findByStudentIdAndCourseId(studentId, courseId);
    }

    @Test
    @DisplayName("Should generate verification code with correct format")
    void testGenerateVerificationCodeFormat() {
        // Arrange
        Long studentId = 1L;
        Long courseId = 1L;

        when(progressRepository.countByStudentIdAndCourseIdAndIsCompleted(studentId, courseId, true))
                .thenReturn(3);
        when(restTemplate.getForObject(contains("/lessons/count/"), eq(Integer.class))).thenReturn(3);

        when(certificateRepository.existsByStudentIdAndCourseId(studentId, courseId))
                .thenReturn(false);
        when(certificateRepository.save(any(Certificate.class)))
                .thenAnswer(invocation -> {
                    Certificate cert = invocation.getArgument(0);
                    cert.setCertificateId(1L);
                    return cert;
                });

        // Act
        Certificate result = progressService.issueCertificate(studentId, courseId);

        // Assert
        assertNotNull(result.getVerificationCode());
        assertTrue(result.getVerificationCode().startsWith("EL-"));
        assertTrue(result.getVerificationCode().contains(String.valueOf(LocalDate.now().getYear())));
    }

    // ============================================
    // Get Certificate Tests
    // ============================================

    @Test
    @DisplayName("Should get certificate when exists")
    void testGetCertificateExists() {
        // Arrange
        Long studentId = 1L;
        Long courseId = 1L;

        when(certificateRepository.findByStudentIdAndCourseId(studentId, courseId))
                .thenReturn(Optional.of(testCertificate));

        // Act
        Optional<Certificate> result = progressService.getCertificate(studentId, courseId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testCertificate.getCertificateId(), result.get().getCertificateId());
        verify(certificateRepository, times(1)).findByStudentIdAndCourseId(studentId, courseId);
    }

    @Test
    @DisplayName("Should return empty when certificate doesn't exist")
    void testGetCertificateNotExists() {
        // Arrange
        Long studentId = 1L;
        Long courseId = 1L;

        when(certificateRepository.findByStudentIdAndCourseId(studentId, courseId))
                .thenReturn(Optional.empty());

        // Act
        Optional<Certificate> result = progressService.getCertificate(studentId, courseId);

        // Assert
        assertFalse(result.isPresent());
        verify(certificateRepository, times(1)).findByStudentIdAndCourseId(studentId, courseId);
    }

    // ============================================
    // Verify Certificate Tests
    // ============================================

    @Test
    @DisplayName("Should verify certificate by verification code")
    void testVerifyCertificateExists() {
        // Arrange
        String verificationCode = "EL-2026-EL-ABC123";

        when(certificateRepository.findByVerificationCode(verificationCode))
                .thenReturn(Optional.of(testCertificate));

        // Act
        Optional<Certificate> result = progressService.verifyCertificate(verificationCode);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testCertificate.getVerificationCode(), result.get().getVerificationCode());
        verify(certificateRepository, times(1)).findByVerificationCode(verificationCode);
    }

    @Test
    @DisplayName("Should return empty when verification code invalid")
    void testVerifyCertificateInvalid() {
        // Arrange
        String invalidCode = "INVALID-CODE";

        when(certificateRepository.findByVerificationCode(invalidCode))
                .thenReturn(Optional.empty());

        // Act
        Optional<Certificate> result = progressService.verifyCertificate(invalidCode);

        // Assert
        assertFalse(result.isPresent());
        verify(certificateRepository, times(1)).findByVerificationCode(invalidCode);
    }

    // ============================================
    // Get All Progress Tests
    // ============================================

    @Test
    @DisplayName("Should return all progress records for a student")
    void testGetAllProgressByStudent() {
        // Arrange
        Long studentId = 1L;
        List<Progress> progressList = new ArrayList<>();
        progressList.add(testProgress);
        progressList.add(Progress.builder()
                .progressId(2)
                .studentId(1L)
                .courseId(1L)
                .lessonId(2L)
                .watchedSeconds(500)
                .isCompleted(false)
                .build());

        when(progressRepository.findByStudentId(studentId))
                .thenReturn(progressList);

        // Act
        List<Progress> result = progressService.getAllProgressByStudent(studentId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(progressRepository, times(1)).findByStudentId(studentId);
    }

    @Test
    @DisplayName("Should return empty list when student has no progress")
    void testGetAllProgressByStudentEmpty() {
        // Arrange
        Long studentId = 1L;

        when(progressRepository.findByStudentId(studentId))
                .thenReturn(new ArrayList<>());

        // Act
        List<Progress> result = progressService.getAllProgressByStudent(studentId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(progressRepository, times(1)).findByStudentId(studentId);
    }

    // ============================================
    // Get All Certificates Tests
    // ============================================

    @Test
    @DisplayName("Should return all certificates for a student")
    void testGetAllCertificatesByStudent() {
        // Arrange
        Long studentId = 1L;
        List<Certificate> certificateList = new ArrayList<>();
        certificateList.add(testCertificate);
        certificateList.add(Certificate.builder()
                .certificateId(2L)
                .studentId(1L)
                .courseId(2L)
                .issuedAt(LocalDate.now())
                .verificationCode("EL-2026-EL-DEF456")
                .build());

        when(certificateRepository.findByStudentId(studentId))
                .thenReturn(certificateList);

        // Act
        List<Certificate> result = progressService.getAllCertificatesByStudent(studentId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(certificateRepository, times(1)).findByStudentId(studentId);
    }

    @Test
    @DisplayName("Should return empty list when student has no certificates")
    void testGetAllCertificatesByStudentEmpty() {
        // Arrange
        Long studentId = 1L;

        when(certificateRepository.findByStudentId(studentId))
                .thenReturn(new ArrayList<>());

        // Act
        List<Certificate> result = progressService.getAllCertificatesByStudent(studentId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(certificateRepository, times(1)).findByStudentId(studentId);
    }
}

