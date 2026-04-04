package com.edulearn.course.repository;

import com.edulearn.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {

    // Find courses by title
    List<Course> findByTitle(String title);

    // Find courses by category
    List<Course> findByCategory(String category);

    // Find courses by instructor
    List<Course> findByInstructorId(Integer instructorId);

    // Find courses by level
    List<Course> findByLevel(String level);

    // Find courses by publication status
    List<Course> findByIsPublished(Boolean isPublished);

    // Find courses with price less than or equal to
    List<Course> findByPriceLessThanEqual(Double price);

    // Search by keyword in title AND description using JPQL
    @Query("SELECT c FROM Course c WHERE c.isPublished = true AND (LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Course> searchByKeyword(@Param("keyword") String keyword);

    // Get top 6 featured (published) courses
    @Query("SELECT c FROM Course c WHERE c.isPublished = true ORDER BY c.createdAt DESC LIMIT 6")
    List<Course> getFeaturedCourses();
}

