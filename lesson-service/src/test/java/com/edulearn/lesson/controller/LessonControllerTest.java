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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.edulearn.lesson.exception.GlobalExceptionHandler;
import com.edulearn.lesson.service.FileStorageService;
import com.edulearn.lesson.entity.Resource;
import org.springframework.test.util.ReflectionTestUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@ExtendWith(MockitoExtension.class)
@DisplayName("Lesson Controller Tests")
class LessonControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LessonService lessonService;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private LessonController lessonController;

    private ObjectMapper objectMapper;
    private Lesson testLesson;

    @BeforeEach
    void setUp() throws Exception {
        String baseUploadPath = "target/test-uploads";
        ReflectionTestUtils.setField(lessonController, "uploadPath", baseUploadPath);
        
        // Create test directories and files
        Path coursePath = Paths.get(baseUploadPath, "course1");
        Files.createDirectories(coursePath);
        Files.write(coursePath.resolve("video.mp4"), "test video content".getBytes());
        Files.write(coursePath.resolve("doc.pdf"), "test pdf content".getBytes());

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

    // ✅ UPDATE
    @Test
    void testUpdateLesson() throws Exception {
        when(lessonService.updateLesson(eq(1), any(Lesson.class))).thenReturn(testLesson);

        mockMvc.perform(put("/api/v1/lessons/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testLesson)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lesson updated successfully"));
    }

    // ✅ REORDER
    @Test
    void testReorderLessons() throws Exception {
        doNothing().when(lessonService).reorderLessons(eq(5), anyList());

        mockMvc.perform(put("/api/v1/lessons/reorder/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(1, 2, 3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Lessons reordered successfully"));
    }

    // ✅ ADD RESOURCE
    @Test
    void testAddResource() throws Exception {
        Resource resource = new Resource();
        resource.setName("Handout");
        resource.setFileUrl("handout.pdf");
        resource.setFileType("PDF");
        resource.setSizeKb(100L);
        
        when(lessonService.addResource(eq(1), any(Resource.class))).thenReturn(resource);

        mockMvc.perform(post("/api/v1/lessons/1/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Resource added successfully"));
    }

    // ✅ REMOVE RESOURCE
    @Test
    void testRemoveResource() throws Exception {
        doNothing().when(lessonService).removeResource(1);

        mockMvc.perform(delete("/api/v1/lessons/resources/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Resource removed successfully"));
    }

    // ✅ GET PREVIEW
    @Test
    void testGetPreviewLessons() throws Exception {
        when(lessonService.getPreviewLessons(5)).thenReturn(List.of(testLesson));

        mockMvc.perform(get("/api/v1/lessons/preview/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    // ✅ GET COUNT
    @Test
    void testGetLessonCount() throws Exception {
        when(lessonService.countLessonsByCourse(5)).thenReturn(10);

        mockMvc.perform(get("/api/v1/lessons/count/5"))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));
    }

    // ✅ UPLOAD VIDEO - Success
    @Test
    void testUploadVideoSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "test data".getBytes());
        when(fileStorageService.storeVideo(any(), anyInt())).thenReturn("http://storage/video.mp4");

        mockMvc.perform(multipart("/api/v1/lessons/upload-video")
                .file(file)
                .param("courseId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.url").value("http://storage/video.mp4"));
    }

    // ✅ UPLOAD VIDEO - Empty File
    @Test
    void testUploadVideoEmpty() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "", "video/mp4", new byte[0]);

        mockMvc.perform(multipart("/api/v1/lessons/upload-video")
                .file(file)
                .param("courseId", "5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No file provided"));
    }

    // ✅ UPLOAD VIDEO - Wrong Type
    @Test
    void testUploadVideoWrongType() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", "data".getBytes());

        mockMvc.perform(multipart("/api/v1/lessons/upload-video")
                .file(file)
                .param("courseId", "5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only video files are allowed"));
    }

    // ✅ UPLOAD VIDEO - Exception
    @Test
    void testUploadVideoException() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "data".getBytes());
        when(fileStorageService.storeVideo(any(), anyInt())).thenThrow(new RuntimeException("Storage full"));

        mockMvc.perform(multipart("/api/v1/lessons/upload-video")
                .file(file)
                .param("courseId", "5"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(containsString("Upload failed")));
    }

    // ✅ UPLOAD PDF - Success
    @Test
    void testUploadPdfSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "pdf data".getBytes());
        when(fileStorageService.storePdf(any(), anyInt())).thenReturn("http://storage/test.pdf");

        mockMvc.perform(multipart("/api/v1/lessons/upload-pdf")
                .file(file)
                .param("courseId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ✅ UPLOAD PDF - Empty
    @Test
    void testUploadPdfEmpty() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "", "application/pdf", new byte[0]);

        mockMvc.perform(multipart("/api/v1/lessons/upload-pdf")
                .file(file)
                .param("courseId", "5"))
                .andExpect(status().isBadRequest());
    }

    // ✅ UPLOAD PDF - Wrong Type
    @Test
    void testUploadPdfWrongType() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "data".getBytes());

        mockMvc.perform(multipart("/api/v1/lessons/upload-pdf")
                .file(file)
                .param("courseId", "5"))
                .andExpect(status().isBadRequest());
    }

    // ✅ UPLOAD PDF - Exception
    @Test
    void testUploadPdfException() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "data".getBytes());
        when(fileStorageService.storePdf(any(), anyInt())).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(multipart("/api/v1/lessons/upload-pdf")
                .file(file)
                .param("courseId", "5"))
                .andExpect(status().isInternalServerError());
    }

    // ✅ SERVE VIDEO - Success
    @Test
    void testServeVideoSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/lessons/videos/course1/video.mp4"))
                .andExpect(status().isOk())
                .andExpect(header().string("Accept-Ranges", "bytes"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.valueOf("video/mp4")));
    }

    // ✅ SERVE VIDEO - Forbidden Path Traversal
    @Test
    void testServeVideoForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/lessons/videos/../secret.txt"))
                .andExpect(status().isForbidden());
    }

    // ✅ SERVE VIDEO - Not Found
    @Test
    void testServeVideoNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/lessons/videos/course1/nonexistent.mp4"))
                .andExpect(status().isNotFound());
    }

    // ✅ SERVE FILE - Success
    @Test
    void testServeFileSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/lessons/files/course1/doc.pdf"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF));
    }

    // ✅ SERVE FILE - Forbidden
    @Test
    void testServeFileForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/lessons/files/../../etc/passwd"))
                .andExpect(status().isForbidden());
    }

    // ✅ SERVE FILE - Not Found
    @Test
    void testServeFileNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/lessons/files/course1/nonexistent.pdf"))
                .andExpect(status().isNotFound());
    }
}