package com.edulearn.progress.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.edulearn.progress.entity.Certificate;

/**
 * Integration tests for CertificateRepository
 * Tests database operations for Certificate entity
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CertificateRepository Test Suite")
class CertificateRepositoryTest {

    @Autowired
    private CertificateRepository certificateRepository;

    private Certificate testCertificate1;
    private Certificate testCertificate2;
    private Certificate testCertificate3;

    @BeforeEach
    void setUp() {
        certificateRepository.deleteAll();

        // Test data setup
        testCertificate1 = Certificate.builder()
                .studentId(1)
                .courseId(1)
                .issuedAt(LocalDate.now())
                .certificateUrl("src/main/resources/certificates/cert_1_1.pdf")
                .verificationCode("EL-2026-EL-ABC123")
                .instructorName("Instructor 1")
                .courseName("Course 1")
                .build();

        testCertificate2 = Certificate.builder()
                .studentId(1)
                .courseId(2)
                .issuedAt(LocalDate.now())
                .certificateUrl("src/main/resources/certificates/cert_1_2.pdf")
                .verificationCode("EL-2026-EL-DEF456")
                .instructorName("Instructor 2")
                .courseName("Course 2")
                .build();

        testCertificate3 = Certificate.builder()
                .studentId(2)
                .courseId(1)
                .issuedAt(LocalDate.now())
                .certificateUrl("src/main/resources/certificates/cert_2_1.pdf")
                .verificationCode("EL-2026-EL-GHI789")
                .instructorName("Instructor 1")
                .courseName("Course 1")
                .build();
    }

    // ============================================
    // Save and Retrieve Tests
    // ============================================

    @Test
    @DisplayName("Should save certificate successfully")
    void testSaveCertificate() {
        // Act
        Certificate savedCert = certificateRepository.save(testCertificate1);

        // Assert
        assertNotNull(savedCert.getCertificateId());
        assertEquals(1, savedCert.getStudentId());
        assertEquals(1, savedCert.getCourseId());
        assertEquals("EL-2026-EL-ABC123", savedCert.getVerificationCode());
    }

    @Test
    @DisplayName("Should retrieve certificate by ID")
    void testFindCertificateById() {
        // Arrange
        Certificate savedCert = certificateRepository.save(testCertificate1);

        // Act
        Optional<Certificate> retrievedCert = certificateRepository.findById(savedCert.getCertificateId());

        // Assert
        assertTrue(retrievedCert.isPresent());
        assertEquals(savedCert.getCertificateId(), retrievedCert.get().getCertificateId());
        assertEquals(savedCert.getVerificationCode(), retrievedCert.get().getVerificationCode());
    }

    // ============================================
    // Find By Student ID Tests
    // ============================================

    @Test
    @DisplayName("Should find all certificates for a student")
    void testFindByStudentId() {
        // Arrange
        certificateRepository.save(testCertificate1);
        certificateRepository.save(testCertificate2);
        certificateRepository.save(testCertificate3);

        // Act
        List<Certificate> results = certificateRepository.findByStudentId(1);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(c -> c.getStudentId() == 1));
    }

    @Test
    @DisplayName("Should return empty list when student has no certificates")
    void testFindByStudentIdEmpty() {
        // Arrange
        certificateRepository.save(testCertificate1);

        // Act
        List<Certificate> results = certificateRepository.findByStudentId(999);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should return only specific student's certificates")
    void testFindByStudentIdMultipleStudents() {
        // Arrange
        certificateRepository.save(testCertificate1);
        certificateRepository.save(testCertificate2);
        certificateRepository.save(testCertificate3);

        // Act
        List<Certificate> student1Certs = certificateRepository.findByStudentId(1);
        List<Certificate> student2Certs = certificateRepository.findByStudentId(2);

        // Assert
        assertEquals(2, student1Certs.size());
        assertEquals(1, student2Certs.size());
    }

    // ============================================
    // Find By Student ID and Course ID Tests
    // ============================================

    @Test
    @DisplayName("Should find certificate by student ID and course ID")
    void testFindByStudentIdAndCourseId() {
        // Arrange
        certificateRepository.save(testCertificate1);

        // Act
        Optional<Certificate> result = certificateRepository.findByStudentIdAndCourseId(1, 1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getStudentId());
        assertEquals(1, result.get().getCourseId());
    }

    @Test
    @DisplayName("Should return empty when certificate not found by student ID and course ID")
    void testFindByStudentIdAndCourseIdNotFound() {
        // Arrange
        certificateRepository.save(testCertificate1);

        // Act
        Optional<Certificate> result = certificateRepository.findByStudentIdAndCourseId(999, 999);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should find correct certificate when multiple exist")
    void testFindByStudentIdAndCourseIdCorrectMatch() {
        // Arrange
        certificateRepository.save(testCertificate1);
        certificateRepository.save(testCertificate2);
        certificateRepository.save(testCertificate3);

        // Act
        Optional<Certificate> result = certificateRepository.findByStudentIdAndCourseId(1, 2);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getStudentId());
        assertEquals(2, result.get().getCourseId());
        assertEquals("EL-2026-EL-DEF456", result.get().getVerificationCode());
    }

    // ============================================
    // Find By Verification Code Tests
    // ============================================

    @Test
    @DisplayName("Should find certificate by verification code")
    void testFindByVerificationCode() {
        // Arrange
        certificateRepository.save(testCertificate1);

        // Act
        Optional<Certificate> result = certificateRepository.findByVerificationCode("EL-2026-EL-ABC123");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("EL-2026-EL-ABC123", result.get().getVerificationCode());
        assertEquals(1, result.get().getStudentId());
        assertEquals(1, result.get().getCourseId());
    }

    @Test
    @DisplayName("Should return empty when verification code not found")
    void testFindByVerificationCodeNotFound() {
        // Arrange
        certificateRepository.save(testCertificate1);

        // Act
        Optional<Certificate> result = certificateRepository.findByVerificationCode("INVALID-CODE");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should find correct certificate by unique verification code")
    void testFindByUniqueVerificationCode() {
        // Arrange
        certificateRepository.save(testCertificate1);
        certificateRepository.save(testCertificate2);
        certificateRepository.save(testCertificate3);

        // Act
        Optional<Certificate> result1 = certificateRepository.findByVerificationCode("EL-2026-EL-ABC123");
        Optional<Certificate> result2 = certificateRepository.findByVerificationCode("EL-2026-EL-GHI789");

        // Assert
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertEquals(1, result1.get().getStudentId());
        assertEquals(2, result2.get().getStudentId());
        assertNotEquals(result1.get().getCertificateId(), result2.get().getCertificateId());
    }

    // ============================================
    // Exists Tests
    // ============================================

    @Test
    @DisplayName("Should return true when certificate exists for student and course")
    void testExistsByStudentIdAndCourseId() {
        // Arrange
        certificateRepository.save(testCertificate1);

        // Act
        boolean exists = certificateRepository.existsByStudentIdAndCourseId(1, 1);

        // Assert
        assertTrue(exists);
    }

    @Test
    @DisplayName("Should return false when certificate doesn't exist")
    void testExistsByStudentIdAndCourseIdNotExists() {
        // Arrange
        certificateRepository.save(testCertificate1);

        // Act
        boolean exists = certificateRepository.existsByStudentIdAndCourseId(999, 999);

        // Assert
        assertFalse(exists);
    }

    @Test
    @DisplayName("Should correctly distinguish between existing and non-existing certificates")
    void testExistsWithMultipleCertificates() {
        // Arrange
        certificateRepository.save(testCertificate1);
        certificateRepository.save(testCertificate2);

        // Act & Assert
        assertTrue(certificateRepository.existsByStudentIdAndCourseId(1, 1));
        assertTrue(certificateRepository.existsByStudentIdAndCourseId(1, 2));
        assertFalse(certificateRepository.existsByStudentIdAndCourseId(1, 3));
        assertFalse(certificateRepository.existsByStudentIdAndCourseId(2, 2));
    }

    // ============================================
    // Update Tests
    // ============================================

    @Test
    @DisplayName("Should update certificate record")
    void testUpdateCertificate() {
        // Arrange
        Certificate savedCert = certificateRepository.save(testCertificate1);

        // Act
        savedCert.setInstructorName("Updated Instructor");
        savedCert.setCourseName("Updated Course");
        Certificate updatedCert = certificateRepository.save(savedCert);

        // Assert
        Optional<Certificate> retrievedCert = certificateRepository.findById(updatedCert.getCertificateId());
        assertTrue(retrievedCert.isPresent());
        assertEquals("Updated Instructor", retrievedCert.get().getInstructorName());
        assertEquals("Updated Course", retrievedCert.get().getCourseName());
    }

    // ============================================
    // Delete Tests
    // ============================================

    @Test
    @DisplayName("Should delete certificate by ID")
    void testDeleteCertificate() {
        // Arrange
        Certificate savedCert = certificateRepository.save(testCertificate1);
        Integer certId = savedCert.getCertificateId();

        // Act
        certificateRepository.deleteById(certId);

        // Assert
        Optional<Certificate> retrievedCert = certificateRepository.findById(certId);
        assertFalse(retrievedCert.isPresent());
    }

    @Test
    @DisplayName("Should delete all certificates")
    void testDeleteAllCertificates() {
        // Arrange
        certificateRepository.save(testCertificate1);
        certificateRepository.save(testCertificate2);
        certificateRepository.save(testCertificate3);

        // Act
        certificateRepository.deleteAll();

        // Assert
        assertEquals(0, certificateRepository.count());
    }

    // ============================================
    // Count Tests
    // ============================================

    @Test
    @DisplayName("Should count total certificates in repository")
    void testCountCertificates() {
        // Arrange
        certificateRepository.save(testCertificate1);
        certificateRepository.save(testCertificate2);
        certificateRepository.save(testCertificate3);

        // Act
        long count = certificateRepository.count();

        // Assert
        assertEquals(3, count);
    }

    // ============================================
    // Data Consistency Tests
    // ============================================

    @Test
    @DisplayName("Should maintain data consistency across operations")
    void testDataConsistency() {
        // Arrange & Act
        Certificate saved1 = certificateRepository.save(testCertificate1);
        Certificate saved2 = certificateRepository.save(testCertificate2);
        Certificate saved3 = certificateRepository.save(testCertificate3);

        // Assert all are retrievable
        assertTrue(certificateRepository.findById(saved1.getCertificateId()).isPresent());
        assertTrue(certificateRepository.findById(saved2.getCertificateId()).isPresent());
        assertTrue(certificateRepository.findById(saved3.getCertificateId()).isPresent());

        // Assert counts are correct
        assertEquals(2, certificateRepository.findByStudentId(1).size());
        assertEquals(1, certificateRepository.findByStudentId(2).size());

        // Assert unique verification codes
        assertTrue(certificateRepository.findByVerificationCode("EL-2026-EL-ABC123").isPresent());
        assertTrue(certificateRepository.findByVerificationCode("EL-2026-EL-DEF456").isPresent());
        assertTrue(certificateRepository.findByVerificationCode("EL-2026-EL-GHI789").isPresent());
    }
}

