package com.edulearn.lesson.repository;

import com.edulearn.lesson.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    List<Lesson> findByCourseId(Integer courseId);

    List<Lesson> findByCourseIdOrderByOrderIndex(Integer courseId);

    List<Lesson> findByIsPreview(Boolean isPreview);

    int countByCourseId(Integer courseId);
}
