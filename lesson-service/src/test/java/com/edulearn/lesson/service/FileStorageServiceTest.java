package com.edulearn.lesson.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("FileStorageService Unit Tests")
class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService();
        ReflectionTestUtils.setField(fileStorageService, "uploadPath", tempDir.toString());
    }

    @Test
    @DisplayName("Should store video file and return URL")
    void testStoreVideo() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test-video.mp4", "video/mp4", "video content".getBytes());

        String resultUrl = fileStorageService.storeVideo(file, 101);

        assertNotNull(resultUrl);
        assertTrue(resultUrl.contains("/api/v1/lessons/videos/course-101/"));
        assertTrue(resultUrl.endsWith(".mp4"));

        Path courseDir = tempDir.resolve("course-101");
        assertTrue(Files.exists(courseDir));
        assertEquals(1, Files.list(courseDir).count());
    }

    @Test
    @DisplayName("Should store PDF file and return URL")
    void testStorePdf() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test-doc.pdf", "application/pdf", "pdf content".getBytes());

        String resultUrl = fileStorageService.storePdf(file, 101);

        assertNotNull(resultUrl);
        assertTrue(resultUrl.contains("/api/v1/lessons/files/pdfs/course-101/"));
        assertTrue(resultUrl.endsWith(".pdf"));

        Path pdfDir = tempDir.resolve("pdfs").resolve("course-101");
        assertTrue(Files.exists(pdfDir));
        assertEquals(1, Files.list(pdfDir).count());
    }

    @Test
    @DisplayName("Should handle file with no extension")
    void testStoreFileNoExtension() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "testvideo", "video/mp4", "content".getBytes());

        String resultUrl = fileStorageService.storeVideo(file, 202);

        assertTrue(resultUrl.endsWith(".mp4"));
    }

    @Test
    @DisplayName("Should handle null filename")
    void testStoreFileNullFilename() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", null, "application/pdf", "content".getBytes());

        String resultUrl = fileStorageService.storePdf(file, 303);

        assertTrue(resultUrl.endsWith(".pdf"));
    }
}
