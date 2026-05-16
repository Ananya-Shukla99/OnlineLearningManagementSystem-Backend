package com.edulearn.course.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.edulearn.course.entity.Course;
import com.edulearn.course.entity.Review;
import com.edulearn.course.repository.CourseRepository;
import com.edulearn.course.repository.ReviewRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewController Unit Tests")
class ReviewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private ReviewController reviewController;

    private ObjectMapper objectMapper;
    private Review testReview;
    private Course testCourse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController).build();
        objectMapper = new ObjectMapper();

        testReview = new Review();
        testReview.setReviewId(1L);
        testReview.setCourseId(1L);
        testReview.setStudentId(1L);
        testReview.setRating(5);
        testReview.setComment("Great course!");

        testCourse = new Course();
        testCourse.setCourseId(1);
        testCourse.setTitle("Test Course");
    }

    // ─── POST /courses/{courseId}/reviews ──────────────────────────────

    @Nested
    @DisplayName("POST /courses/{courseId}/reviews")
    class SubmitReview {

        @Test
        @DisplayName("Should submit review successfully")
        void testSubmitReviewSuccess() throws Exception {
            when(reviewRepository.findByStudentIdAndCourseId(anyLong(), anyLong()))
                    .thenReturn(Optional.empty());
            when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
            when(courseRepository.findById(1)).thenReturn(Optional.of(testCourse));
            when(reviewRepository.findAverageRatingByCourseId(1L)).thenReturn(5.0);
            when(reviewRepository.countByCourseId(1L)).thenReturn(1L);

            mockMvc.perform(post("/api/v1/courses/1/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testReview)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Review submitted successfully"))
                    .andExpect(jsonPath("$.data.rating").value(5));
        }

        @Test
        @DisplayName("Should return 409 if student already reviewed")
        void testSubmitReviewAlreadyExists() throws Exception {
            when(reviewRepository.findByStudentIdAndCourseId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(testReview));

            mockMvc.perform(post("/api/v1/courses/1/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testReview)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("You have already reviewed this course"));
        }

        @Test
        @DisplayName("Should return 400 when rating is below 1")
        void testSubmitReviewRatingTooLow() throws Exception {
            Review badReview = new Review();
            badReview.setStudentId(2L);
            badReview.setRating(0);
            badReview.setComment("Bad");

            when(reviewRepository.findByStudentIdAndCourseId(anyLong(), anyLong()))
                    .thenReturn(Optional.empty());

            mockMvc.perform(post("/api/v1/courses/1/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(badReview)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Rating must be between 1 and 5"));
        }

        @Test
        @DisplayName("Should return 400 when rating is above 5")
        void testSubmitReviewRatingTooHigh() throws Exception {
            Review badReview = new Review();
            badReview.setStudentId(2L);
            badReview.setRating(6);
            badReview.setComment("Too high");

            when(reviewRepository.findByStudentIdAndCourseId(anyLong(), anyLong()))
                    .thenReturn(Optional.empty());

            mockMvc.perform(post("/api/v1/courses/1/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(badReview)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Rating must be between 1 and 5"));
        }

        @Test
        @DisplayName("Should update course rating when course exists")
        void testSubmitReviewUpdatesCourseRating() throws Exception {
            when(reviewRepository.findByStudentIdAndCourseId(anyLong(), anyLong()))
                    .thenReturn(Optional.empty());
            when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
            when(courseRepository.findById(1)).thenReturn(Optional.of(testCourse));
            when(reviewRepository.findAverageRatingByCourseId(1L)).thenReturn(4.5);
            when(reviewRepository.countByCourseId(1L)).thenReturn(10L);

            mockMvc.perform(post("/api/v1/courses/1/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testReview)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should handle null average rating")
        void testSubmitReviewNullAvgRating() throws Exception {
            when(reviewRepository.findByStudentIdAndCourseId(anyLong(), anyLong()))
                    .thenReturn(Optional.empty());
            when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
            when(courseRepository.findById(1)).thenReturn(Optional.of(testCourse));
            when(reviewRepository.findAverageRatingByCourseId(1L)).thenReturn(null);
            when(reviewRepository.countByCourseId(1L)).thenReturn(null);

            mockMvc.perform(post("/api/v1/courses/1/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testReview)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should handle missing course gracefully")
        void testSubmitReviewCourseNotFound() throws Exception {
            when(reviewRepository.findByStudentIdAndCourseId(anyLong(), anyLong()))
                    .thenReturn(Optional.empty());
            when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
            when(courseRepository.findById(1)).thenReturn(Optional.empty());
            when(reviewRepository.findAverageRatingByCourseId(1L)).thenReturn(5.0);
            when(reviewRepository.countByCourseId(1L)).thenReturn(1L);

            mockMvc.perform(post("/api/v1/courses/1/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testReview)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    // ─── GET /courses/{courseId}/reviews ───────────────────────────────

    @Nested
    @DisplayName("GET /courses/{courseId}/reviews")
    class GetCourseReviews {

        @Test
        @DisplayName("Should return course reviews")
        void testGetCourseReviews() throws Exception {
            when(reviewRepository.findByCourseId(1L)).thenReturn(List.of(testReview));

            mockMvc.perform(get("/api/v1/courses/1/reviews"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].rating").value(5));
        }

        @Test
        @DisplayName("Should return empty list when no reviews")
        void testGetCourseReviewsEmpty() throws Exception {
            when(reviewRepository.findByCourseId(999L)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/courses/999/reviews"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("Should return 500 on exception")
        void testGetCourseReviewsError() throws Exception {
            when(reviewRepository.findByCourseId(1L))
                    .thenThrow(new RuntimeException("DB error"));

            mockMvc.perform(get("/api/v1/courses/1/reviews"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("DB error"));
        }
    }

    // ─── GET /courses/{courseId}/rating ────────────────────────────────

    @Nested
    @DisplayName("GET /courses/{courseId}/rating")
    class GetCourseRating {

        @Test
        @DisplayName("Should return average rating and count")
        void testGetCourseRating() throws Exception {
            when(reviewRepository.findAverageRatingByCourseId(1L)).thenReturn(4.5);
            when(reviewRepository.countByCourseId(1L)).thenReturn(10L);

            mockMvc.perform(get("/api/v1/courses/1/rating"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.averageRating").value(4.5))
                    .andExpect(jsonPath("$.ratingCount").value(10));
        }

        @Test
        @DisplayName("Should handle null average rating")
        void testGetCourseRatingNull() throws Exception {
            when(reviewRepository.findAverageRatingByCourseId(999L)).thenReturn(null);
            when(reviewRepository.countByCourseId(999L)).thenReturn(null);

            mockMvc.perform(get("/api/v1/courses/999/rating"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.averageRating").value(0.0))
                    .andExpect(jsonPath("$.ratingCount").value(0));
        }

        @Test
        @DisplayName("Should return 500 on exception")
        void testGetCourseRatingError() throws Exception {
            when(reviewRepository.findAverageRatingByCourseId(1L))
                    .thenThrow(new RuntimeException("DB error"));

            mockMvc.perform(get("/api/v1/courses/1/rating"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
