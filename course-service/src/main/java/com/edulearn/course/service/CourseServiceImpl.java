package com.edulearn.course.service;

import com.edulearn.course.entity.Course;
import com.edulearn.course.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseRepository courseRepository;

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
        return courseRepository.save(course);
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
        courseToPublish.setIsPublished(true);
        courseRepository.save(courseToPublish);
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
