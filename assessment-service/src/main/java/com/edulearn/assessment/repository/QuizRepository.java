package com.edulearn.assessment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.edulearn.assessment.entity.Quiz;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Integer> {

    List<Quiz> findByCourseId(Integer courseId);

    List<Quiz> findByLessonId(Integer lessonId);

    List<Quiz> findByIsPublished(Boolean isPublished);

    int countByCourseId(Integer courseId);
}

