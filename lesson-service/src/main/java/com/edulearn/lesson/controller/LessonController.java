package com.edulearn.lesson.controller;

import com.edulearn.lesson.entity.Lesson;
import com.edulearn.lesson.entity.Resource;
import com.edulearn.lesson.service.FileStorageService;
import com.edulearn.lesson.service.LessonService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/lessons")
public class LessonController {

    @Autowired
    private LessonService lessonService;

    @Autowired
    private FileStorageService fileStorageService;

    @Value("${app.upload.path}")
    private String uploadPath;

    @PostMapping
    public ResponseEntity<Map<String, Object>> addLesson(@RequestBody Lesson lesson) {
        Lesson savedLesson = lessonService.addLesson(lesson);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true, "message", "Lesson created successfully", "data", savedLesson));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<Map<String, Object>> getLessonsByCourse(@PathVariable Integer courseId) {
        return ResponseEntity.ok(Map.of("success", true, "data", lessonService.getLessonsByCourse(courseId)));
    }

    @GetMapping("/{lessonId}")
    public ResponseEntity<Map<String, Object>> getLessonById(@PathVariable Integer lessonId) {
        Lesson lesson = lessonService.getLessonById(lessonId);
        return ResponseEntity.ok(Map.of("success", true, "data", lesson));
    }

    @PutMapping("/{lessonId}")
    public ResponseEntity<Map<String, Object>> updateLesson(@PathVariable Integer lessonId,
            @RequestBody Lesson lesson) {
        Lesson updatedLesson = lessonService.updateLesson(lessonId, lesson);
        return ResponseEntity
                .ok(Map.of("success", true, "message", "Lesson updated successfully", "data", updatedLesson));
    }

    @DeleteMapping("/{lessonId}")
    public ResponseEntity<Map<String, Object>> deleteLesson(@PathVariable Integer lessonId) {
        lessonService.deleteLesson(lessonId);
        return ResponseEntity.ok(Map.of("success", true, "message", "Lesson deleted successfully"));
    }

    @PutMapping("/reorder/{courseId}")
    public ResponseEntity<Map<String, Object>> reorderLessons(@PathVariable Integer courseId,
            @RequestBody List<Integer> lessonIds) {
        lessonService.reorderLessons(courseId, lessonIds);
        return ResponseEntity.ok(Map.of("success", true, "message", "Lessons reordered successfully"));
    }

    @PostMapping("/{lessonId}/resources")
    public ResponseEntity<Map<String, Object>> addResource(@PathVariable Integer lessonId,
            @RequestBody Resource resource) {
        Resource savedResource = lessonService.addResource(lessonId, resource);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true, "message", "Resource added successfully", "data", savedResource));
    }

    @DeleteMapping("/resources/{resourceId}")
    public ResponseEntity<Map<String, Object>> removeResource(@PathVariable Integer resourceId) {
        lessonService.removeResource(resourceId);
        return ResponseEntity.ok(Map.of("success", true, "message", "Resource removed successfully"));
    }

    @GetMapping("/preview/{courseId}")
    public ResponseEntity<Map<String, Object>> getPreviewLessons(@PathVariable Integer courseId) {
        return ResponseEntity.ok(Map.of("success", true, "data", lessonService.getPreviewLessons(courseId)));
    }

    @GetMapping("/count/{courseId}")
    public ResponseEntity<Integer> getLessonCount(@PathVariable Integer courseId) {
        return ResponseEntity.ok(lessonService.countLessonsByCourse(courseId));
    }

    @PostMapping("/upload-video")
    public ResponseEntity<Map<String, Object>> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("courseId") Integer courseId) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "No file provided"));
            }
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Only video files are allowed"));
            }

            String url = fileStorageService.storeVideo(file, courseId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Video uploaded successfully",
                    "url", url,
                    "fileName", file.getOriginalFilename(),
                    "sizeKb", file.getSize() / 1024));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Upload failed: " + ex.getMessage()));
        }
    }

    @PostMapping("/upload-pdf")
    public ResponseEntity<Map<String, Object>> uploadPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("courseId") Integer courseId) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "No file provided"));
            }
            String contentType = file.getContentType();
            if (contentType == null || !contentType.equals("application/pdf")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Only PDF files are allowed"));
            }

            String url = fileStorageService.storePdf(file, courseId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "PDF uploaded successfully",
                    "url", url,
                    "fileName", file.getOriginalFilename(),
                    "sizeKb", file.getSize() / 1024));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Upload failed: " + ex.getMessage()));
        }
    }

    @GetMapping("/videos/**")
    public ResponseEntity<org.springframework.core.io.Resource> serveVideo(HttpServletRequest request) {
        try {
            String requestPath = request.getRequestURI().replace("/api/v1/lessons/videos/", "");
            Path uploadRoot = Paths.get(uploadPath).normalize();
            Path filePath = uploadRoot.resolve(requestPath).normalize();
            if (!filePath.startsWith(uploadRoot)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            org.springframework.core.io.Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "video/mp4";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .body(resource);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/files/**")
    public ResponseEntity<org.springframework.core.io.Resource> serveFile(HttpServletRequest request) {
        try {
            String requestPath = request.getRequestURI().replace("/api/v1/lessons/files/", "");
            Path uploadRoot = Paths.get(uploadPath).normalize();
            Path filePath = uploadRoot.resolve(requestPath).normalize();
            if (!filePath.startsWith(uploadRoot)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            org.springframework.core.io.Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
