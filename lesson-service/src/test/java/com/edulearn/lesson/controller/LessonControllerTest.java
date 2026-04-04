package com.edulearn.lesson.controller;

import com.edulearn.lesson.entity.Lesson;
import com.edulearn.lesson.entity.Resource;
import com.edulearn.lesson.service.LessonService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Lesson Controller Tests")
class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LessonService lessonService;

    @Autowired
    private ObjectMapper objectMapper;

    private Lesson testLesson;
    private Resource testResource;

    @BeforeEach
    void setUp() {
        testLesson = new Lesson();
        testLesson.setLessonId(1);
        testLesson.setCourseId(5);
        testLesson.setTitle("Java Basics");
        testLesson.setContentType("VIDEO");
        testLesson.setContentUrl("https://example.com/video.mp4");
        testLesson.setDurationMinutes(30);
        testLesson.setOrderIndex(0);
        testLesson.setDescription("Learn Java fundamentals");
        testLesson.setIsPreview(false);

        testResource = new Resource();
        testResource.setResourceId(1);
        testResource.setLessonId(1);
        testResource.setName("Java Cheat Sheet");
        testResource.setFileUrl("https://example.com/cheatsheet.pdf");
        testResource.setFileType("PDF");
        testResource.setSizeKb(500L);
    }

    // ==================== POST /api/v1/lessons Tests ====================

    @Test
    @DisplayName("Should create lesson with INSTRUCTOR role")
    @WithMockUser(username = "instructor@test.com", authorities = "INSTRUCTOR")
    void testAddLessonAsInstructor() throws Exception {
        // Arrange
        when(lessonService.addLesson(any(Lesson.class))).thenReturn(testLesson);

        // Act & Assert
        mockMvc.perform(post("/api/v1/lessons")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testLesson)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lesson created successfully"))
                .andExpect(jsonPath("$.data.lessonId").value(1))
                .andExpect(jsonPath("$.data.title").value("Java Basics"));

        verify(lessonService, times(1)).addLesson(any(Lesson.class));
    }

    @Test
    @DisplayName("Should reject lesson creation with STUDENT role")
    @WithMockUser(username = "student@test.com", authorities = "STUDENT")
    void testAddLessonAsStudent() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/lessons")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testLesson)))
                .andExpect(status().isForbidden());

        verify(lessonService, never()).addLesson(any(Lesson.class));
    }


    // ==================== GET /api/v1/lessons/course/{courseId} Tests ====================

    @Test
    @DisplayName("Should get lessons by course as authenticated user")
    @WithMockUser(username = "student@test.com", authorities = "STUDENT")
    void testGetLessonsByCourse() throws Exception {
        // Arrange
        Lesson lesson1 = new Lesson(1, 5, "Lesson 1", "VIDEO", "url1", 20, 0, "Desc1", false);
        Lesson lesson2 = new Lesson(2, 5, "Lesson 2", "ARTICLE", "url2", 15, 1, "Desc2", false);
        List<Lesson> lessons = Arrays.asList(lesson1, lesson2);

        when(lessonService.getLessonsByCourse(5)).thenReturn(lessons);

        // Act & Assert
        mockMvc.perform(get("/api/v1/lessons/course/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].title").value("Lesson 1"))
                .andExpect(jsonPath("$.data[1].title").value("Lesson 2"));

        verify(lessonService, times(1)).getLessonsByCourse(5);
    }

 
    // ==================== GET /api/v1/lessons/{lessonId} Tests ====================

    @Test
    @DisplayName("Should get lesson by ID with authentication")
    @WithMockUser(username = "student@test.com", authorities = "STUDENT")
    void testGetLessonById() throws Exception {
        // Arrange
        when(lessonService.getLessonById(anyInt(), anyInt())).thenReturn(testLesson);

        // Act & Assert
        mockMvc.perform(get("/api/v1/lessons/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.lessonId").value(1))
                .andExpect(jsonPath("$.data.title").value("Java Basics"));

        verify(lessonService, times(1)).getLessonById(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Should return 403 when accessing paid lesson without enrollment")
    @WithMockUser(username = "student@test.com", authorities = "STUDENT")
    void testGetLessonByIdAccessDenied() throws Exception {
        // Arrange
        when(lessonService.getLessonById(anyInt(), anyInt()))
                .thenThrow(new org.springframework.security.access.AccessDeniedException("Please enroll to access this lesson"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/lessons/1"))
                .andExpect(status().isForbidden());
    }

    // ==================== PUT /api/v1/lessons/{lessonId} Tests ====================

    @Test
    @DisplayName("Should update lesson with INSTRUCTOR role")
    @WithMockUser(username = "instructor@test.com", authorities = "INSTRUCTOR")
    void testUpdateLessonAsInstructor() throws Exception {
        // Arrange
        Lesson updatedLesson = new Lesson(1, 5, "Updated Title", "ARTICLE", "newurl", 45, 0, "Updated Desc", false);
        when(lessonService.updateLesson(anyInt(), any(Lesson.class))).thenReturn(updatedLesson);

        // Act & Assert
        mockMvc.perform(put("/api/v1/lessons/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedLesson)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lesson updated successfully"))
                .andExpect(jsonPath("$.data.title").value("Updated Title"));

        verify(lessonService, times(1)).updateLesson(1, updatedLesson);
    }

    @Test
    @DisplayName("Should reject lesson update with STUDENT role")
    @WithMockUser(username = "student@test.com", authorities = "STUDENT")
    void testUpdateLessonAsStudent() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/v1/lessons/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testLesson)))
                .andExpect(status().isForbidden());

        verify(lessonService, never()).updateLesson(anyInt(), any(Lesson.class));
    }

    // ==================== DELETE /api/v1/lessons/{lessonId} Tests ====================

    @Test
    @DisplayName("Should delete lesson with INSTRUCTOR role")
    @WithMockUser(username = "instructor@test.com", authorities = "INSTRUCTOR")
    void testDeleteLessonAsInstructor() throws Exception {
        // Arrange
        doNothing().when(lessonService).deleteLesson(1);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/lessons/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lesson deleted successfully"));

        verify(lessonService, times(1)).deleteLesson(1);
    }

    @Test
    @DisplayName("Should reject lesson deletion with STUDENT role")
    @WithMockUser(username = "student@test.com", authorities = "STUDENT")
    void testDeleteLessonAsStudent() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/lessons/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(lessonService, never()).deleteLesson(anyInt());
    }

    // ==================== PUT /api/v1/lessons/reorder/{courseId} Tests ====================

    @Test
    @DisplayName("Should reorder lessons with INSTRUCTOR role")
    @WithMockUser(username = "instructor@test.com", authorities = "INSTRUCTOR")
    void testReorderLessonsAsInstructor() throws Exception {
        // Arrange
        List<Integer> lessonIds = Arrays.asList(3, 1, 2);
        doNothing().when(lessonService).reorderLessons(anyInt(), any());

        // Act & Assert
        mockMvc.perform(put("/api/v1/lessons/reorder/5")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lessonIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lessons reordered successfully"));

        verify(lessonService, times(1)).reorderLessons(5, lessonIds);
    }

    @Test
    @DisplayName("Should reject reordering with STUDENT role")
    @WithMockUser(username = "student@test.com", authorities = "STUDENT")
    void testReorderLessonsAsStudent() throws Exception {
        // Arrange
        List<Integer> lessonIds = Arrays.asList(3, 1, 2);

        // Act & Assert
        mockMvc.perform(put("/api/v1/lessons/reorder/5")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lessonIds)))
                .andExpect(status().isForbidden());

        verify(lessonService, never()).reorderLessons(anyInt(), any());
    }

    // ==================== POST /api/v1/lessons/{lessonId}/resources Tests ====================

    @Test
    @DisplayName("Should add resource with INSTRUCTOR role")
    @WithMockUser(username = "instructor@test.com", authorities = "INSTRUCTOR")
    void testAddResourceAsInstructor() throws Exception {
        // Arrange
        Resource savedResource = new Resource(1, 1, "Java Cheat Sheet", "https://example.com/cheatsheet.pdf", "PDF", 500L);

        when(lessonService.addResource(anyInt(), any(Resource.class))).thenReturn(savedResource);

        // Act & Assert
        mockMvc.perform(post("/api/v1/lessons/1/resources")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(savedResource)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Resource added successfully"))
                .andExpect(jsonPath("$.data.name").value("Java Cheat Sheet"));

        verify(lessonService, times(1)).addResource(anyInt(), any(Resource.class));
    }

    @Test
    @DisplayName("Should reject resource addition with STUDENT role")
    @WithMockUser(username = "student@test.com", authorities = "STUDENT")
    void testAddResourceAsStudent() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/lessons/1/resources")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testResource)))
                .andExpect(status().isForbidden());

        verify(lessonService, never()).addResource(anyInt(), any(Resource.class));
    }

    // ==================== DELETE /api/v1/lessons/resources/{resourceId} Tests ====================

    @Test
    @DisplayName("Should delete resource with INSTRUCTOR role")
    @WithMockUser(username = "instructor@test.com", authorities = "INSTRUCTOR")
    void testDeleteResourceAsInstructor() throws Exception {
        // Arrange
        doNothing().when(lessonService).removeResource(1);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/lessons/resources/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Resource removed successfully"));

        verify(lessonService, times(1)).removeResource(1);
    }

    @Test
    @DisplayName("Should reject resource deletion with STUDENT role")
    @WithMockUser(username = "student@test.com", authorities = "STUDENT")
    void testDeleteResourceAsStudent() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/lessons/resources/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(lessonService, never()).removeResource(anyInt());
    }

    // ==================== GET /api/v1/lessons/preview/{courseId} Tests ====================

    @Test
    @DisplayName("Should get preview lessons without authentication")
    void testGetPreviewLessonsNoAuth() throws Exception {
        // Arrange
        Lesson lesson1 = new Lesson(1, 5, "Preview 1", "VIDEO", "url1", 20, 0, "Desc1", true);
        Lesson lesson2 = new Lesson(2, 5, "Preview 2", "ARTICLE", "url2", 15, 1, "Desc2", true);
        List<Lesson> lessons = Arrays.asList(lesson1, lesson2);

        when(lessonService.getPreviewLessons(5)).thenReturn(lessons);

        // Act & Assert
        mockMvc.perform(get("/api/v1/lessons/preview/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].title").value("Preview 1"))
                .andExpect(jsonPath("$.data[1].title").value("Preview 2"));

        verify(lessonService, times(1)).getPreviewLessons(5);
    }

    @Test
    @DisplayName("Should get preview lessons with authentication")
    @WithMockUser(username = "student@test.com", authorities = "STUDENT")
    void testGetPreviewLessonsWithAuth() throws Exception {
        // Arrange
        Lesson lesson1 = new Lesson(1, 5, "Preview 1", "VIDEO", "url1", 20, 0, "Desc1", true);
        List<Lesson> lessons = Arrays.asList(lesson1);

        when(lessonService.getPreviewLessons(5)).thenReturn(lessons);

        // Act & Assert
        mockMvc.perform(get("/api/v1/lessons/preview/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)));

        verify(lessonService, times(1)).getPreviewLessons(5);
    }

    @Test
    @DisplayName("Should return empty list when no preview lessons")
    void testGetPreviewLessonsEmpty() throws Exception {
        // Arrange
        when(lessonService.getPreviewLessons(5)).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/lessons/preview/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(0)));

        verify(lessonService, times(1)).getPreviewLessons(5);
    }
}

