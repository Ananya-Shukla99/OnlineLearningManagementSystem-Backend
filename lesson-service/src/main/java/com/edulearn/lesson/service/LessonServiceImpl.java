package com.edulearn.lesson.service;

import com.edulearn.lesson.entity.Lesson;
import com.edulearn.lesson.entity.Resource;
import com.edulearn.lesson.repository.LessonRepository;
import com.edulearn.lesson.repository.ResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LessonServiceImpl implements LessonService {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    // For enrollment checking and user fetching (will be injected later)
    // @Autowired
    // private EnrollmentService enrollmentService;
    // @Autowired
    // private UserRepository userRepository;

    @Override
    public Lesson addLesson(Lesson lesson) {
        return lessonRepository.save(lesson);
    }

    @Override
    public List<Lesson> getLessonsByCourse(Integer courseId) {
        return lessonRepository.findByCourseIdOrderByOrderIndex(courseId);
    }

    @Override
    public Lesson getLessonById(Integer lessonId, Integer studentId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found with ID: " + lessonId));

        // ACCESS GATE: Check if lesson is preview or student is enrolled
        if (!lesson.getIsPreview()) {
            // Lesson is paid - check enrollment (will be implemented in Part 3)
            // String email = SecurityContextHolder.getContext().getAuthentication().getName();
            // User user = userRepository.findByEmail(email)
            //         .orElseThrow(() -> new RuntimeException("User not found"));
            // if (!enrollmentService.isEnrolled(user.getUserId(), lesson.getCourseId())) {
            //     throw new AccessDeniedException("Please enroll to access this lesson");
            // }
        }

        return lesson;
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
        // Delete all resources first
        List<Resource> resources = resourceRepository.findByLessonId(lessonId);
        resourceRepository.deleteAll(resources);

        // Then delete the lesson
        lessonRepository.deleteById(lessonId);
    }

    @Override
    @Transactional
    public void reorderLessons(Integer courseId, List<Integer> lessonIds) {
        for (int i = 0; i < lessonIds.size(); i++) {
            final int index = i; // Create final copy for lambda
            Lesson lesson = lessonRepository.findById(lessonIds.get(index))
                    .orElseThrow(() -> new RuntimeException("Lesson not found with ID: " + lessonIds.get(index)));
            lesson.setOrderIndex(index);
            lessonRepository.save(lesson);
        }
    }

    @Override
    public Resource addResource(Integer lessonId, Resource resource) {
        // Verify lesson exists
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
        List<Lesson> allLessons = lessonRepository.findByCourseIdOrderByOrderIndex(courseId);
        return allLessons.stream()
                .filter(lesson -> lesson.getIsPreview())
                .toList();
    }
}
