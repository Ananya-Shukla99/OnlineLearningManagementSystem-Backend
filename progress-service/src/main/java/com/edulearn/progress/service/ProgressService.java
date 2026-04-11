package com.edulearn.progress.service;

import java.util.List;
import java.util.Optional;
import com.edulearn.progress.entity.Progress;
import com.edulearn.progress.entity.Certificate;

/**
 * Service interface for Progress operations
 * Defines contract for tracking lesson completion and certificate generation
 */
public interface ProgressService {

    void trackProgress(Integer studentId, Integer courseId, Integer lessonId, Integer watchedSeconds);

    void markLessonComplete(Integer studentId, Integer courseId, Integer lessonId);

    Integer getCourseProgress(Integer studentId, Integer courseId);

    Optional<Progress> getLessonProgress(Integer studentId, Integer lessonId);

    Certificate issueCertificate(Integer studentId, Integer courseId);

    Optional<Certificate> getCertificate(Integer studentId, Integer courseId);

    Optional<Certificate> verifyCertificate(String verificationCode);

    List<Progress> getAllProgressByStudent(Integer studentId);

    List<Certificate> getAllCertificatesByStudent(Integer studentId);
}

