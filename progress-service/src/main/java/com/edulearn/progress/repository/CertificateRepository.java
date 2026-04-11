package com.edulearn.progress.repository;

import com.edulearn.progress.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Certificate entity
 * Handles all database operations for certificates
 */
@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Integer> {

    List<Certificate> findByStudentId(Integer studentId);

    Optional<Certificate> findByStudentIdAndCourseId(Integer studentId, Integer courseId);

    Optional<Certificate> findByVerificationCode(String verificationCode);

    boolean existsByStudentIdAndCourseId(Integer studentId, Integer courseId);
}

