package com.edulearn.lesson.controller;

import com.edulearn.lesson.entity.Lesson;
import com.edulearn.lesson.service.LessonService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.edulearn.lesson.exception.GlobalExceptionHandler;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Lesson Controller Tests")
class LessonControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LessonService lessonService;

    @InjectMocks
    private LessonController lessonController;

    private ObjectMapper objectMapper;
    private Lesson testLesson;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(lessonController, "uploadPath", "target/uploads");
        
        mockMvc = MockMvcBuilders
                .standaloneSetup(lessonController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        testLesson = new Lesson();
        testLesson.setLessonId(1);
        testLesson.setCourseId(5);
        testLesson.setTitle("Intro to Java");
    }

    // ✅ GET by course
    @Test
    void testGetByCourse() throws Exception {
        when(lessonService.getLessonsByCourse(5)).thenReturn(List.of(testLesson));

        mockMvc.perform(get("/api/v1/lessons/course/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].lessonId").value(1));
    }

    // ✅ GET by ID
    @Test
    void testGetById() throws Exception {
        when(lessonService.getLessonById(1)).thenReturn(testLesson);

        mockMvc.perform(get("/api/v1/lessons/1"))
                .andExpect(status().isOk());
    }

    // ❗ FIXED: Not Found case (important for Sonar)
    @Test
    void testGetByIdNotFound() throws Exception {
        when(lessonService.getLessonById(999))
                .thenThrow(new RuntimeException("Lesson not found"));

        mockMvc.perform(get("/api/v1/lessons/999"))
                .andExpect(status().isNotFound());
    }

    // ✅ CREATE
    @Test
    void testCreate() throws Exception {
        when(lessonService.addLesson(any(Lesson.class))).thenReturn(testLesson);

        mockMvc.perform(post("/api/v1/lessons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testLesson)))
                .andExpect(status().isCreated());
    }

    // ✅ DELETE
    @Test
    void testDelete() throws Exception {
        doNothing().when(lessonService).deleteLesson(1);

        mockMvc.perform(delete("/api/v1/lessons/1"))
                .andExpect(status().isOk());
    }

    // ❗ FIXED: Delete Not Found
    @Test
    void testDeleteNotFound() throws Exception {
        doThrow(new RuntimeException("Lesson not found"))
                .when(lessonService).deleteLesson(999);

        mockMvc.perform(delete("/api/v1/lessons/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/lessons/videos/** - File not found")
    void testServeVideoNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/lessons/videos/nonexistent.mp4"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/lessons/files/** - File not found")
    void testServeFileNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/lessons/files/nonexistent.pdf"))
                .andExpect(status().isNotFound());
    }
}