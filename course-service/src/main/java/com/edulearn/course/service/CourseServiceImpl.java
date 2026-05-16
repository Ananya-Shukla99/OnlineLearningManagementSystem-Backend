package com.edulearn.course.service;

import com.edulearn.course.entity.Course;
import com.edulearn.course.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.client.RestTemplate;
import com.edulearn.notification.dto.NotificationDto;
import com.edulearn.notification.config.RabbitMQConfig;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Resolves an instructor's full name from the auth-service.
     * Falls back to "Instructor #ID" if the auth-service is unreachable.
     */
    private String resolveInstructorName(Integer instructorId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(
                    "http://localhost:8081/auth/user/" + instructorId, Map.class);
            if (response != null && response.get("data") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> user = (Map<String, Object>) response.get("data");
                String name = (String) user.get("fullName");
                if (name != null && !name.isBlank()) return name;
            }
        } catch (Exception e) {
            System.err.println("[CourseService] Could not resolve instructor name for ID "
                    + instructorId + ": " + e.getMessage());
        }
        return "Instructor #" + instructorId;
    }

    @Override
    public Course createCourse(Course course) {
        if (course.getTitle() == null || course.getTitle().isEmpty()) {
            throw new RuntimeException("Course title is required");
        }
        if (course.getInstructorId() == null) {
            throw new RuntimeException("Instructor ID is required");
        }
        if (course.getPrice() == null || course.getPrice() < 0) {
            throw new RuntimeException("Course price must be non-negative");
        }
        course.setApprovalStatus("DRAFT");
        course.setIsPublished(false);
        
        // Set default values for mandatory fields if not provided
        if (course.getCategory() == null) {
            course.setCategory("Development");
        }
        if (course.getLevel() == null) {
            course.setLevel("Beginner");
        }
        if (course.getTotalDuration() == null) {
            course.setTotalDuration(0);
        }
        if (course.getLanguage() == null) {
            course.setLanguage("English");
        }
        
        Course savedCourse = courseRepository.save(course);
        
        // Notify admin via RabbitMQ — include instructor name in the message
        try {
            String instructorName = resolveInstructorName(savedCourse.getInstructorId());
            NotificationDto adminNotif = new NotificationDto();
            adminNotif.setUserId(1L); // Signals the notification service to broadcast to all admins
            adminNotif.setType("COURSE_CREATED_ADMIN");
            adminNotif.setTitle("New Course Added");
            adminNotif.setMessage(instructorName + " added a new course: '" + savedCourse.getTitle() + "'.");
            adminNotif.setRelatedEntityId(Long.valueOf(savedCourse.getCourseId()));
            adminNotif.setRelatedEntityType("COURSE");
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, adminNotif);
            System.out.println("[CourseService] Admin notification queued for course '" + savedCourse.getTitle() + "' by " + instructorName);
        } catch (Exception e) {
            System.err.println("[CourseService] Failed to send course-creation admin notification: " + e.getMessage());
        }
        
        return savedCourse;
    }

    @Override
    public List<Course> getAllCourses() {
        // Return only published courses for students
        return courseRepository.findByIsPublished(true);
    }

    @Override
    public Optional<Course> getCourseById(Integer id) {
        return courseRepository.findById(id);
    }

    @Override
    public List<Course> getCoursesByCategory(String category) {
        List<Course> courses = courseRepository.findByCategory(category);
        // Filter only published courses
        return courses.stream().filter(Course::getIsPublished).toList();
    }

    @Override
    public List<Course> getCoursesByInstructor(Integer instructorId) {
        return courseRepository.findByInstructorId(instructorId);
    }

    @Override
    public List<Course> searchCourses(String keyword) {
        return courseRepository.searchByKeyword(keyword);
    }

    @Override
    public Course updateCourse(Integer id, Course course) {
        Optional<Course> existingCourse = courseRepository.findById(id);

        if (existingCourse.isEmpty()) {
            throw new RuntimeException("Course not found with ID: " + id);
        }

        Course courseToUpdate = existingCourse.get();

        if (course.getTitle() != null) {
            courseToUpdate.setTitle(course.getTitle());
        }
        if (course.getDescription() != null) {
            courseToUpdate.setDescription(course.getDescription());
        }
        if (course.getCategory() != null) {
            courseToUpdate.setCategory(course.getCategory());
        }
        if (course.getLevel() != null) {
            courseToUpdate.setLevel(course.getLevel());
        }
        if (course.getPrice() != null) {
            courseToUpdate.setPrice(course.getPrice());
        }
        if (course.getThumbnailUrl() != null) {
            courseToUpdate.setThumbnailUrl(course.getThumbnailUrl());
        }
        if (course.getTotalDuration() != null) {
            courseToUpdate.setTotalDuration(course.getTotalDuration());
        }
        if (course.getLanguage() != null) {
            courseToUpdate.setLanguage(course.getLanguage());
        }

        return courseRepository.save(courseToUpdate);
    }

    @Override
    public void publishCourse(Integer id) {
        Optional<Course> course = courseRepository.findById(id);

        if (course.isEmpty()) {
            throw new RuntimeException("Course not found with ID: " + id);
        }

        Course courseToPublish = course.get();
        courseToPublish.setIsPublished(false);
        courseToPublish.setApprovalStatus("PENDING_APPROVAL");
        courseToPublish.setRejectionReason(null);
        courseRepository.save(courseToPublish);

        // Notify admin via RabbitMQ — include instructor name in the message
        try {
            String instructorName = resolveInstructorName(courseToPublish.getInstructorId());
            NotificationDto adminNotif = new NotificationDto();
            adminNotif.setUserId(1L); // Signals the notification service to broadcast to all admins
            adminNotif.setType("COURSE_PENDING");
            adminNotif.setTitle("Course Pending Approval");
            adminNotif.setMessage(instructorName + " submitted '" + courseToPublish.getTitle() + "' for approval. Please review.");
            adminNotif.setRelatedEntityId(Long.valueOf(courseToPublish.getCourseId()));
            adminNotif.setRelatedEntityType("COURSE");
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, adminNotif);
            System.out.println("[CourseService] Pending-approval notification queued for '" + courseToPublish.getTitle() + "' by " + instructorName);
        } catch (Exception e) {
            System.err.println("[CourseService] Failed to send pending-approval admin notification: " + e.getMessage());
        }
    }

    @Override
    public void approveCourse(Integer id) {
        Optional<Course> course = courseRepository.findById(id);
        if (course.isEmpty()) {
            throw new RuntimeException("Course not found with ID: " + id);
        }
        Course approvedCourse = course.get();
        approvedCourse.setApprovalStatus("APPROVED");
        approvedCourse.setIsPublished(true);
        approvedCourse.setRejectionReason(null);
        courseRepository.save(approvedCourse);
    }

    @Override
    public void rejectCourse(Integer id, String reason) {
        Optional<Course> course = courseRepository.findById(id);
        if (course.isEmpty()) {
            throw new RuntimeException("Course not found with ID: " + id);
        }
        Course rejectedCourse = course.get();
        rejectedCourse.setApprovalStatus("REJECTED");
        rejectedCourse.setIsPublished(false);
        rejectedCourse.setRejectionReason(reason);
        courseRepository.save(rejectedCourse);
    }

    @Override
    public List<Course> getPendingCourses() {
        return courseRepository.findByApprovalStatus("PENDING_APPROVAL");
    }

    @Override
    public void deleteCourse(Integer id) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("Course not found with ID: " + id);
        }
        courseRepository.deleteById(id);
    }

    @Override
    public List<Course> getFeaturedCourses() {
        return courseRepository.getFeaturedCourses();
    }

    @Override
    public List<Course> getAllCoursesIncludingUnpublished() {
        // Return all courses (admin access)
        return courseRepository.findAll();
    }
}
