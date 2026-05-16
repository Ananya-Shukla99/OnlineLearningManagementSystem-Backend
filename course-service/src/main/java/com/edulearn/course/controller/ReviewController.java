package com.edulearn.course.controller;

import com.edulearn.course.entity.Course;
import com.edulearn.course.entity.Review;
import com.edulearn.course.repository.CourseRepository;
import com.edulearn.course.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/courses")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CourseRepository courseRepository;

    /**
     * POST /courses/{courseId}/reviews — submit a review (authenticated)
     */
    @PostMapping("/{courseId}/reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> submitReview(@PathVariable Long courseId,
                                                            @RequestBody Review review) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Check if student already reviewed this course
            Optional<Review> existing = reviewRepository.findByStudentIdAndCourseId(
                    review.getStudentId(), courseId);
            if (existing.isPresent()) {
                response.put("success", false);
                response.put("message", "You have already reviewed this course");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            // Validate rating
            if (review.getRating() < 1 || review.getRating() > 5) {
                response.put("success", false);
                response.put("message", "Rating must be between 1 and 5");
                return ResponseEntity.badRequest().body(response);
            }

            review.setCourseId(courseId);
            Review savedReview = reviewRepository.save(review);

            // Recalculate course average rating
            Double avgRating = reviewRepository.findAverageRatingByCourseId(courseId);
            Long count = reviewRepository.countByCourseId(courseId);

            Optional<Course> courseOpt = courseRepository.findById(courseId.intValue());
            if (courseOpt.isPresent()) {
                Course course = courseOpt.get();
                course.setAverageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
                course.setRatingCount(count != null ? count.intValue() : 0);
                courseRepository.save(course);
            }

            response.put("success", true);
            response.put("message", "Review submitted successfully");
            response.put("data", savedReview);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * GET /courses/{courseId}/reviews — list all reviews for a course (public)
     */
    @GetMapping("/{courseId}/reviews")
    public ResponseEntity<Map<String, Object>> getCourseReviews(@PathVariable Long courseId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Review> reviews = reviewRepository.findByCourseId(courseId);
            response.put("success", true);
            response.put("data", reviews);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * GET /courses/{courseId}/rating — get average rating (public)
     */
    @GetMapping("/{courseId}/rating")
    public ResponseEntity<Map<String, Object>> getCourseRating(@PathVariable Long courseId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Double avgRating = reviewRepository.findAverageRatingByCourseId(courseId);
            Long count = reviewRepository.countByCourseId(courseId);
            response.put("success", true);
            response.put("averageRating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
            response.put("ratingCount", count != null ? count.intValue() : 0);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
