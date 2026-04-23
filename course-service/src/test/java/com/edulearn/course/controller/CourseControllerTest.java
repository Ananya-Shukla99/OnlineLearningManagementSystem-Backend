package com.edulearn.course.controller;

import com.edulearn.course.entity.Course;
import com.edulearn.course.service.CourseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Course Controller Tests")
class CourseControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CourseService courseService;

    @InjectMocks
    private CourseController courseController;

    private ObjectMapper objectMapper;
    private Course testCourse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(courseController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testCourse = new Course();
        testCourse.setCourseId(1);
        testCourse.setTitle("Test Course");
        testCourse.setDescription("A comprehensive test course");
        testCourse.setCategory("Programming");
        testCourse.setLevel("Beginner");
        testCourse.setPrice(99.99);
        testCourse.setInstructorId(101);
        testCourse.setTotalDuration(300);
        testCourse.setIsPublished(true);
        testCourse.setApprovalStatus("APPROVED");
        testCourse.setLanguage("English");
    }

    // ─── GET /courses ─────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/courses")
    class GetAllCourses {

        @Test
        @DisplayName("Should return all published courses")
        void testGetAllSuccess() throws Exception {
            when(courseService.getAllCourses()).thenReturn(List.of(testCourse));

            mockMvc.perform(get("/api/v1/courses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Courses retrieved successfully"))
                    .andExpect(jsonPath("$.data[0].title").value("Test Course"))
                    .andExpect(jsonPath("$.data[0].courseId").value(1));

            verify(courseService).getAllCourses();
        }

        @Test
        @DisplayName("Should return empty list when no courses")
        void testGetAllEmpty() throws Exception {
            when(courseService.getAllCourses()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/courses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("Should return 500 on service exception")
        void testGetAllException() throws Exception {
            when(courseService.getAllCourses()).thenThrow(new RuntimeException("DB error"));

            mockMvc.perform(get("/api/v1/courses"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("DB error"));
        }
    }

    // ─── GET /courses/{id} ────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/courses/{id}")
    class GetCourseById {

        @Test
        @DisplayName("Should return course when found")
        void testGetByIdSuccess() throws Exception {
            when(courseService.getCourseById(1)).thenReturn(Optional.of(testCourse));

            mockMvc.perform(get("/api/v1/courses/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.courseId").value(1))
                    .andExpect(jsonPath("$.data.title").value("Test Course"));
        }

        @Test
        @DisplayName("Should return 404 when course not found")
        void testGetByIdNotFound() throws Exception {
            when(courseService.getCourseById(999)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/courses/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Course not found"));
        }

        @Test
        @DisplayName("Should return 500 on exception")
        void testGetByIdException() throws Exception {
            when(courseService.getCourseById(1)).thenThrow(new RuntimeException("DB error"));

            mockMvc.perform(get("/api/v1/courses/1"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ─── GET /courses/search ──────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/courses/search")
    class SearchCourses {

        @Test
        @DisplayName("Should return matching courses")
        void testSearchSuccess() throws Exception {
            when(courseService.searchCourses("java")).thenReturn(List.of(testCourse));

            mockMvc.perform(get("/api/v1/courses/search").param("keyword", "java"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Search completed successfully"))
                    .andExpect(jsonPath("$.data[0].title").value("Test Course"));
        }

        @Test
        @DisplayName("Should return empty for no matches")
        void testSearchNoResults() throws Exception {
            when(courseService.searchCourses("xyz")).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/courses/search").param("keyword", "xyz"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("Should return 500 on exception")
        void testSearchException() throws Exception {
            when(courseService.searchCourses("fail")).thenThrow(new RuntimeException("Search error"));

            mockMvc.perform(get("/api/v1/courses/search").param("keyword", "fail"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ─── GET /courses/category/{category} ─────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/courses/category/{category}")
    class GetByCategory {

        @Test
        @DisplayName("Should return courses in category")
        void testGetByCategorySuccess() throws Exception {
            when(courseService.getCoursesByCategory("Programming")).thenReturn(List.of(testCourse));

            mockMvc.perform(get("/api/v1/courses/category/Programming"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].category").value("Programming"));
        }

        @Test
        @DisplayName("Should return empty for unknown category")
        void testGetByCategoryEmpty() throws Exception {
            when(courseService.getCoursesByCategory("Unknown")).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/courses/category/Unknown"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("Should return 500 on exception")
        void testGetByCategoryException() throws Exception {
            when(courseService.getCoursesByCategory("Bad")).thenThrow(new RuntimeException("Error"));

            mockMvc.perform(get("/api/v1/courses/category/Bad"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ─── GET /courses/featured ────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/courses/featured")
    class GetFeaturedCourses {

        @Test
        @DisplayName("Should return featured courses")
        void testGetFeaturedSuccess() throws Exception {
            when(courseService.getFeaturedCourses()).thenReturn(List.of(testCourse));

            mockMvc.perform(get("/api/v1/courses/featured"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Featured courses retrieved successfully"))
                    .andExpect(jsonPath("$.data[0].courseId").value(1));
        }

        @Test
        @DisplayName("Should return 500 on exception")
        void testGetFeaturedException() throws Exception {
            when(courseService.getFeaturedCourses()).thenThrow(new RuntimeException("Error"));

            mockMvc.perform(get("/api/v1/courses/featured"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ─── GET /courses/instructor/{instructorId} ───────────────────────

    @Nested
    @DisplayName("GET /api/v1/courses/instructor/{instructorId}")
    class GetByInstructor {

        @Test
        @DisplayName("Should return instructor's courses")
        void testGetByInstructorSuccess() throws Exception {
            when(courseService.getCoursesByInstructor(101)).thenReturn(List.of(testCourse));

            mockMvc.perform(get("/api/v1/courses/instructor/101"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].instructorId").value(101));
        }

        @Test
        @DisplayName("Should return empty for unknown instructor")
        void testGetByInstructorEmpty() throws Exception {
            when(courseService.getCoursesByInstructor(999)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/courses/instructor/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("Should return 500 on exception")
        void testGetByInstructorException() throws Exception {
            when(courseService.getCoursesByInstructor(101))
                    .thenThrow(new RuntimeException("Error"));

            mockMvc.perform(get("/api/v1/courses/instructor/101"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ─── POST /courses ────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/v1/courses")
    class CreateCourse {

        @Test
        @DisplayName("Should create course and return 201")
        void testCreateSuccess() throws Exception {
            when(courseService.createCourse(any(Course.class))).thenReturn(testCourse);

            mockMvc.perform(post("/api/v1/courses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testCourse)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Course created successfully"))
                    .andExpect(jsonPath("$.data.title").value("Test Course"));
        }

        @Test
        @DisplayName("Should return 400 on validation error")
        void testCreateValidationError() throws Exception {
            when(courseService.createCourse(any(Course.class)))
                    .thenThrow(new RuntimeException("Course title is required"));

            mockMvc.perform(post("/api/v1/courses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new Course())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Course title is required"));
        }
    }

    // ─── PUT /courses/{id} ────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/v1/courses/{id}")
    class UpdateCourse {

        @Test
        @DisplayName("Should update course successfully")
        void testUpdateSuccess() throws Exception {
            Course updated = new Course();
            updated.setCourseId(1);
            updated.setTitle("Updated Title");
            updated.setPrice(149.99);

            when(courseService.updateCourse(eq(1), any(Course.class))).thenReturn(updated);

            mockMvc.perform(put("/api/v1/courses/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updated)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Course updated successfully"));
        }

        @Test
        @DisplayName("Should return 404 when course not found")
        void testUpdateNotFound() throws Exception {
            when(courseService.updateCourse(eq(999), any(Course.class)))
                    .thenThrow(new RuntimeException("Course not found with ID: 999"));

            mockMvc.perform(put("/api/v1/courses/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testCourse)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ─── PUT /courses/{id}/publish ────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/v1/courses/{id}/publish")
    class PublishCourse {

        @Test
        @DisplayName("Should publish course successfully")
        void testPublishSuccess() throws Exception {
            doNothing().when(courseService).publishCourse(1);

            mockMvc.perform(put("/api/v1/courses/1/publish"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Course submitted for review"));
        }

        @Test
        @DisplayName("Should return 404 when course not found")
        void testPublishNotFound() throws Exception {
            doThrow(new RuntimeException("Course not found with ID: 999"))
                    .when(courseService).publishCourse(999);

            mockMvc.perform(put("/api/v1/courses/999/publish"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ─── PUT /courses/{id}/approve ────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/v1/courses/{id}/approve")
    class ApproveCourse {

        @Test
        @DisplayName("Should approve course successfully")
        void testApproveSuccess() throws Exception {
            doNothing().when(courseService).approveCourse(1);

            mockMvc.perform(put("/api/v1/courses/1/approve"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Course approved and published"));
        }

        @Test
        @DisplayName("Should return 404 when course not found")
        void testApproveNotFound() throws Exception {
            doThrow(new RuntimeException("Course not found with ID: 999"))
                    .when(courseService).approveCourse(999);

            mockMvc.perform(put("/api/v1/courses/999/approve"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ─── PUT /courses/{id}/reject ─────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/v1/courses/{id}/reject")
    class RejectCourse {

        @Test
        @DisplayName("Should reject course with reason")
        void testRejectSuccess() throws Exception {
            doNothing().when(courseService).rejectCourse(eq(1), anyString());

            mockMvc.perform(put("/api/v1/courses/1/reject")
                    .param("reason", "Low quality content"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Course rejected"));
        }

        @Test
        @DisplayName("Should reject with default reason")
        void testRejectDefaultReason() throws Exception {
            doNothing().when(courseService).rejectCourse(eq(1), anyString());

            mockMvc.perform(put("/api/v1/courses/1/reject"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should return 404 when course not found")
        void testRejectNotFound() throws Exception {
            doThrow(new RuntimeException("Course not found"))
                    .when(courseService).rejectCourse(eq(999), anyString());

            mockMvc.perform(put("/api/v1/courses/999/reject"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ─── GET /courses/pending ─────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/courses/pending")
    class GetPendingCourses {

        @Test
        @DisplayName("Should return pending courses")
        void testGetPendingSuccess() throws Exception {
            Course pending = new Course();
            pending.setCourseId(2);
            pending.setApprovalStatus("PENDING_APPROVAL");
            pending.setIsPublished(false);

            when(courseService.getPendingCourses()).thenReturn(List.of(pending));

            mockMvc.perform(get("/api/v1/courses/pending"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].approvalStatus").value("PENDING_APPROVAL"));
        }

        @Test
        @DisplayName("Should return 500 on exception")
        void testGetPendingException() throws Exception {
            when(courseService.getPendingCourses()).thenThrow(new RuntimeException("Error"));

            mockMvc.perform(get("/api/v1/courses/pending"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ─── DELETE /courses/{id} ─────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/v1/courses/{id}")
    class DeleteCourse {

        @Test
        @DisplayName("Should delete course successfully")
        void testDeleteSuccess() throws Exception {
            doNothing().when(courseService).deleteCourse(1);

            mockMvc.perform(delete("/api/v1/courses/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Course deleted successfully"));
        }

        @Test
        @DisplayName("Should return 404 when course not found")
        void testDeleteNotFound() throws Exception {
            doThrow(new RuntimeException("Course not found with ID: 999"))
                    .when(courseService).deleteCourse(999);

            mockMvc.perform(delete("/api/v1/courses/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ─── GET /courses/all ─────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/courses/all")
    class GetAllCoursesAdmin {

        @Test
        @DisplayName("Should return all courses including unpublished")
        void testGetAllAdminSuccess() throws Exception {
            Course unpublished = new Course();
            unpublished.setCourseId(2);
            unpublished.setIsPublished(false);

            when(courseService.getAllCoursesIncludingUnpublished())
                    .thenReturn(List.of(testCourse, unpublished));

            mockMvc.perform(get("/api/v1/courses/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("All courses retrieved successfully"))
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("Should return 500 on exception")
        void testGetAllAdminException() throws Exception {
            when(courseService.getAllCoursesIncludingUnpublished())
                    .thenThrow(new RuntimeException("Error"));

            mockMvc.perform(get("/api/v1/courses/all"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
