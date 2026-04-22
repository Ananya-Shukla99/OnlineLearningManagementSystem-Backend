package com.edulearn.lesson.service;

import com.edulearn.lesson.entity.Lesson;
import com.edulearn.lesson.entity.Resource;
import com.edulearn.lesson.repository.LessonRepository;
import com.edulearn.lesson.repository.ResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LessonServiceImpl implements LessonService {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Override
    public Lesson addLesson(Lesson lesson) {
        if (lesson.getContentType() == null) {
            lesson.setContentType("VIDEO"); // Default type
        }
        if (lesson.getContentUrl() == null) {
            lesson.setContentUrl("https://vimeo.com/76979871"); // Sample content
        }
        if (lesson.getDurationMinutes() == null) {
            lesson.setDurationMinutes(5);
        }
        if (lesson.getOrderIndex() == null) {
            lesson.setOrderIndex(lessonRepository.countByCourseId(lesson.getCourseId()));
        }
        if (lesson.getIsPreview() == null) {
            lesson.setIsPreview(false);
        }
        return lessonRepository.save(lesson);
    }

    @Override
    public List<Lesson> getLessonsByCourse(Integer courseId) {
        return lessonRepository.findByCourseIdOrderByOrderIndex(courseId);
    }

    @Override
    public Lesson getLessonById(Integer lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found with ID: " + lessonId));
    }

    @Override
    public Lesson updateLesson(Integer lessonId, Lesson lesson) {
        Lesson existingLesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found with ID: " + lessonId));

        existingLesson.setTitle(lesson.getTitle());
        existingLesson.setDescription(lesson.getDescription());
        existingLesson.setContentType(lesson.getContentType());
        existingLesson.setContentUrl(lesson.getContentUrl());
        existingLesson.setDurationMinutes(lesson.getDurationMinutes());
        existingLesson.setIsPreview(lesson.getIsPreview());
        return lessonRepository.save(existingLesson);
    }

    @Override
    public void deleteLesson(Integer lessonId) {
        resourceRepository.deleteAll(resourceRepository.findByLessonId(lessonId));
        lessonRepository.deleteById(lessonId);
    }

    @Override
    @Transactional
    public void reorderLessons(Integer courseId, List<Integer> lessonIds) {
        for (int i = 0; i < lessonIds.size(); i++) {
            final int index = i;
            Lesson lesson = lessonRepository.findById(lessonIds.get(index))
                    .orElseThrow(() -> new RuntimeException("Lesson not found with ID: " + lessonIds.get(index)));
            lesson.setOrderIndex(i);
            lessonRepository.save(lesson);
        }
    }

    @Override
    public Resource addResource(Integer lessonId, Resource resource) {
        lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found with ID: " + lessonId));
        resource.setLessonId(lessonId);
        return resourceRepository.save(resource);
    }

    @Override
    public void removeResource(Integer resourceId) {
        resourceRepository.deleteById(resourceId);
    }

    @Override
    public List<Lesson> getPreviewLessons(Integer courseId) {
        return lessonRepository.findByCourseIdOrderByOrderIndex(courseId).stream()
                .filter(Lesson::getIsPreview)
                .toList();
    }

    @Override
    public int countLessonsByCourse(Integer courseId) {
        return lessonRepository.countByCourseId(courseId);
    }

}
