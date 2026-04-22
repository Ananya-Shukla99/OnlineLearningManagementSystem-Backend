package com.edulearn.progress.repository;

import com.edulearn.progress.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Progress entity
 * Handles all database operations for progress tracking
 */
@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {

    List<Progress> findByStudentIdAndCourseId(Long studentId, Long courseId);

    Optional<Progress> findByStudentIdAndLessonId(Long studentId, Long lessonId);

    int countByStudentIdAndCourseIdAndIsCompleted(Long studentId, Long courseId, Boolean isCompleted);

    List<Progress> findByStudentId(Long studentId);
}


