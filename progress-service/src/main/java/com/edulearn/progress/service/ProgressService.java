package com.edulearn.progress.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.edulearn.progress.entity.Progress;
import com.edulearn.progress.entity.Certificate;

/**
 * Service interface for Progress operations
 * Defines contract for tracking lesson completion and certificate generation
 */
public interface ProgressService {

    void trackProgress(Long studentId, Long courseId, Long lessonId, Integer watchedSeconds);

    Map<String, Object> markLessonComplete(Long studentId, Long courseId, Long lessonId);

    Integer getCourseProgress(Long studentId, Long courseId);

    Optional<Progress> getLessonProgress(Long studentId, Long lessonId);

    Certificate issueCertificate(Long studentId, Long courseId);

    Optional<Certificate> getCertificate(Long studentId, Long courseId);

    Optional<Certificate> verifyCertificate(String verificationCode);

    List<Progress> getAllProgressByStudent(Long studentId);

    List<Certificate> getAllCertificatesByStudent(Long studentId);
}

