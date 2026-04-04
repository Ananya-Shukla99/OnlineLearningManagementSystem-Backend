package com.edulearn.lesson.controller;

import com.edulearn.lesson.entity.Lesson;
import com.edulearn.lesson.entity.Resource;
import com.edulearn.lesson.service.LessonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/lessons")
@CrossOrigin(origins = "*")
public class LessonController {

    @Autowired
    private LessonService lessonService;

    // POST /lessons (INSTRUCTOR only)
    @PostMapping
    @PreAuthorize("hasAuthority('INSTRUCTOR')")
    public ResponseEntity<Map<String, Object>> addLesson(@RequestBody Lesson lesson) {
        Lesson savedLesson = lessonService.addLesson(lesson);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("success", true, "message", "Lesson created successfully", "data", savedLesson)
        );
    }

    // GET /lessons/course/{courseId} (authenticated users)
    @GetMapping("/course/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getLessonsByCourse(@PathVariable Integer courseId) {
        List<Lesson> lessons = lessonService.getLessonsByCourse(courseId);
        return ResponseEntity.ok(
                Map.of("success", true, "data", lessons)
        );
    }

    // GET /lessons/{lessonId} (authenticated - access gate applies)
    @GetMapping("/{lessonId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getLessonById(@PathVariable Integer lessonId) {
        try {
            // Get student ID from security context (will be implemented properly later)
            Integer studentId = 1; // Placeholder
            Lesson lesson = lessonService.getLessonById(lessonId, studentId);
            return ResponseEntity.ok(
                    Map.of("success", true, "data", lesson)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("success", false, "error", e.getMessage())
            );
        }
    }

    // PUT /lessons/{lessonId} (INSTRUCTOR only)
    @PutMapping("/{lessonId}")
    @PreAuthorize("hasAuthority('INSTRUCTOR')")
    public ResponseEntity<Map<String, Object>> updateLesson(@PathVariable Integer lessonId, @RequestBody Lesson lesson) {
        Lesson updatedLesson = lessonService.updateLesson(lessonId, lesson);
        return ResponseEntity.ok(
                Map.of("success", true, "message", "Lesson updated successfully", "data", updatedLesson)
        );
    }

    // DELETE /lessons/{lessonId} (INSTRUCTOR only)
    @DeleteMapping("/{lessonId}")
    @PreAuthorize("hasAuthority('INSTRUCTOR')")
    public ResponseEntity<Map<String, Object>> deleteLesson(@PathVariable Integer lessonId) {
        lessonService.deleteLesson(lessonId);
        return ResponseEntity.ok(
                Map.of("success", true, "message", "Lesson deleted successfully")
        );
    }

    // PUT /lessons/reorder/{courseId} (INSTRUCTOR only) body=List<Integer>
    @PutMapping("/reorder/{courseId}")
    @PreAuthorize("hasAuthority('INSTRUCTOR')")
    public ResponseEntity<Map<String, Object>> reorderLessons(@PathVariable Integer courseId, @RequestBody List<Integer> lessonIds) {
        lessonService.reorderLessons(courseId, lessonIds);
        return ResponseEntity.ok(
                Map.of("success", true, "message", "Lessons reordered successfully")
        );
    }

    // POST /lessons/{lessonId}/resources (INSTRUCTOR only)
    @PostMapping("/{lessonId}/resources")
    @PreAuthorize("hasAuthority('INSTRUCTOR')")
    public ResponseEntity<Map<String, Object>> addResource(@PathVariable Integer lessonId, @RequestBody Resource resource) {
        Resource savedResource = lessonService.addResource(lessonId, resource);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("success", true, "message", "Resource added successfully", "data", savedResource)
        );
    }

    // DELETE /lessons/resources/{resourceId} (INSTRUCTOR only)
    @DeleteMapping("/resources/{resourceId}")
    @PreAuthorize("hasAuthority('INSTRUCTOR')")
    public ResponseEntity<Map<String, Object>> removeResource(@PathVariable Integer resourceId) {
        lessonService.removeResource(resourceId);
        return ResponseEntity.ok(
                Map.of("success", true, "message", "Resource removed successfully")
        );
    }

    // GET /lessons/preview/{courseId} (NO AUTH - open to everyone)
    @GetMapping("/preview/{courseId}")
    public ResponseEntity<Map<String, Object>> getPreviewLessons(@PathVariable Integer courseId) {
        List<Lesson> previewLessons = lessonService.getPreviewLessons(courseId);
        return ResponseEntity.ok(
                Map.of("success", true, "data", previewLessons)
        );
    }
}
