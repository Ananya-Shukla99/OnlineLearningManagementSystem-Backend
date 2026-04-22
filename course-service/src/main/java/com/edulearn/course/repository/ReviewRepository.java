package com.edulearn.course.repository;

import com.edulearn.course.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByCourseId(Integer courseId);

    Optional<Review> findByStudentIdAndCourseId(Integer studentId, Integer courseId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.courseId = :courseId")
    Double findAverageRatingByCourseId(@Param("courseId") Integer courseId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.courseId = :courseId")
    Long countByCourseId(@Param("courseId") Integer courseId);
}
