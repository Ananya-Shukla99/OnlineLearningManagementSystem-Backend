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
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Certificate c WHERE c.studentId = :studentId")
    List<Certificate> findByStudentId(@org.springframework.data.repository.query.Param("studentId") Long studentId);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Certificate c WHERE c.studentId = :studentId AND c.courseId = :courseId")
    Optional<Certificate> findByStudentIdAndCourseId(@org.springframework.data.repository.query.Param("studentId") Long studentId, @org.springframework.data.repository.query.Param("courseId") Long courseId);

    Optional<Certificate> findByVerificationCode(String verificationCode);

    @org.springframework.data.jpa.repository.Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Certificate c WHERE c.studentId = :studentId AND c.courseId = :courseId")
    boolean existsByStudentIdAndCourseId(@org.springframework.data.repository.query.Param("studentId") Long studentId, @org.springframework.data.repository.query.Param("courseId") Long courseId);
}

