package com.edulearn.lesson.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.edulearn.lesson.entity.Lesson;
import com.edulearn.lesson.entity.Resource;
import com.edulearn.lesson.repository.LessonRepository;
import com.edulearn.lesson.repository.ResourceRepository;

@Service
public class LessonServiceImpl implements LessonService {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${enrollment.service.url:http://localhost:8084/api/v1}")
    private String enrollmentServiceUrl;

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
            // Lesson is paid - check enrollment via REST call to enrollment-service
            String token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();

            // Call enrollment-service to check if student is enrolled
            boolean isEnrolled = checkEnrollmentViaRest(studentId, lesson.getCourseId(), token);

            if (!isEnrolled) {
                throw new AccessDeniedException("Please enroll to access this lesson");
            }
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

    // Helper method to check enrollment via REST call to enrollment-service
    private boolean checkEnrollmentViaRest(Integer studentId, Integer courseId, String token) {
        try {
            String url = enrollmentServiceUrl + "/enrollments/check?studentId=" + studentId + "&courseId=" + courseId;


            // Create headers with Authorization
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Call enrollment-service
            var response = restTemplate.getForEntity(url, EnrollmentCheckResponse.class, entity);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getData();
            }
            return false;
        } catch (Exception e) {
            // If enrollment service is down, deny access for safety
            System.err.println("Error checking enrollment: " + e.getMessage());
            return false;
        }
    }

    // Inner class for deserialization
    private static class EnrollmentCheckResponse {
        private boolean data;

        public boolean getData() {
            return data;
        }

        public void setData(boolean data) {
            this.data = data;
        }
    }
}
