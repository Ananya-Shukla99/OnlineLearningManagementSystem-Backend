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
public interface ProgressRepository extends JpaRepository<Progress, Integer> {

    List<Progress> findByStudentIdAndCourseId(Integer studentId, Integer courseId);

    Optional<Progress> findByStudentIdAndLessonId(Integer studentId, Integer lessonId);

    int countByStudentIdAndCourseIdAndIsCompleted(Integer studentId, Integer courseId, boolean isCompleted);

    List<Progress> findByStudentId(Integer studentId);
}

