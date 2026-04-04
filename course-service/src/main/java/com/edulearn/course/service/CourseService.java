package com.edulearn.course.service;

import com.edulearn.course.entity.Course;
import java.util.List;
import java.util.Optional;

public interface CourseService {

    // Create a new course
    Course createCourse(Course course);

    // Get all published courses (for students)
    List<Course> getAllCourses();

    // Get course by ID
    Optional<Course> getCourseById(Integer id);

    // Get courses by category
    List<Course> getCoursesByCategory(String category);

    // Get courses by instructor
    List<Course> getCoursesByInstructor(Integer instructorId);

    // Search courses by keyword
    List<Course> searchCourses(String keyword);

    // Update a course
    Course updateCourse(Integer id, Course course);

    // Publish a course (flip isPublished to true)
    void publishCourse(Integer id);

    // Delete a course
    void deleteCourse(Integer id);

    // Get top 6 featured courses
    List<Course> getFeaturedCourses();

    // Get all courses including unpublished (for admin)
    List<Course> getAllCoursesIncludingUnpublished();
}
