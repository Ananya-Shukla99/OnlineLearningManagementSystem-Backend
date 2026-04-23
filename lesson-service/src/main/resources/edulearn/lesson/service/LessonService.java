package com.edulearn.lesson.service;

import com.edulearn.lesson.entity.Lesson;
import com.edulearn.lesson.entity.Resource;
import java.util.List;

public interface LessonService {
    Lesson addLesson(Lesson lesson);

    List<Lesson> getLessonsByCourse(Integer courseId);

    Lesson getLessonById(Integer lessonId, Integer studentId);

    Lesson updateLesson(Integer lessonId, Lesson lesson);

    void deleteLesson(Integer lessonId);

    void reorderLessons(Integer courseId, List<Integer> lessonIds);

    Resource addResource(Integer lessonId, Resource resource);

    void removeResource(Integer resourceId);

    List<Lesson> getPreviewLessons(Integer courseId);
}
