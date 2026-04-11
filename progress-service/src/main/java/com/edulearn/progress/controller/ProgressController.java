package com.edulearn.progress.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.edulearn.progress.entity.Progress;
import com.edulearn.progress.entity.Certificate;
import com.edulearn.progress.service.ProgressService;

/**
 * REST Controller for Progress operations
 * Handles lesson progress tracking and certificate generation
 */
@RestController
@RequestMapping("/api/v1/progress")
@Tag(name = "Progress Service", description = "Student progress tracking and certificate management")
public class ProgressController {

    @Autowired
    private ProgressService progressService;

    @PutMapping("/track")
    @Operation(summary = "Track progress", description = "Update student lesson watch time")
    public ResponseEntity<Void> trackProgress(@RequestBody Map<String, Integer> request) {
        Integer studentId = request.get("studentId");
        Integer courseId = request.get("courseId");
        Integer lessonId = request.get("lessonId");
        Integer watchedSeconds = request.get("watchedSeconds");

        progressService.trackProgress(studentId, courseId, lessonId, watchedSeconds);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/complete")
    @Operation(summary = "Mark lesson complete", description = "Mark lesson as 100% complete")
    public ResponseEntity<Void> markLessonComplete(@RequestBody Map<String, Integer> request) {
        Integer studentId = request.get("studentId");
        Integer courseId = request.get("courseId");
        Integer lessonId = request.get("lessonId");

        progressService.markLessonComplete(studentId, courseId, lessonId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/course")
    @Operation(summary = "Get course progress", description = "Get course completion percentage (0-100)")
    public ResponseEntity<Integer> getCourseProgress(@RequestParam Integer studentId, @RequestParam Integer courseId) {
        Integer progress = progressService.getCourseProgress(studentId, courseId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/lesson")
    @Operation(summary = "Get lesson progress", description = "Get progress details for a specific lesson")
    public ResponseEntity<?> getLessonProgress(@RequestParam Integer studentId, @RequestParam Integer lessonId) {
        Optional<Progress> progress = progressService.getLessonProgress(studentId, lessonId);
        if (progress.isPresent()) {
            return ResponseEntity.ok(progress.get());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/all/{studentId}")
    @Operation(summary = "Get all progress", description = "Get all progress records for a student")
    public ResponseEntity<List<Progress>> getAllProgressByStudent(@PathVariable Integer studentId) {
        List<Progress> progressList = progressService.getAllProgressByStudent(studentId);
        return ResponseEntity.ok(progressList);
    }

    @PostMapping("/certificates/issue")
    @Operation(summary = "Issue certificate", description = "Manually trigger certificate generation")
    public ResponseEntity<Certificate> issueCertificate(@RequestParam Integer studentId, @RequestParam Integer courseId) {
        Certificate certificate = progressService.issueCertificate(studentId, courseId);
        return ResponseEntity.ok(certificate);
    }

    @GetMapping("/certificates/student/{studentId}")
    @Operation(summary = "Get student certificates", description = "Get all certificates issued to a student")
    public ResponseEntity<List<Certificate>> getStudentCertificates(@PathVariable Integer studentId) {
        List<Certificate> certificates = progressService.getAllCertificatesByStudent(studentId);
        return ResponseEntity.ok(certificates);
    }

    @GetMapping("/certificates/verify/{verificationCode}")
    @Operation(summary = "Verify certificate", description = "Verify certificate using verification code (PUBLIC - No Auth)")
    public ResponseEntity<?> verifyCertificate(@PathVariable String verificationCode) {
        Optional<Certificate> certificate = progressService.verifyCertificate(verificationCode);
        if (certificate.isPresent()) {
            return ResponseEntity.ok(certificate.get());
        }
        return ResponseEntity.notFound().build();
    }
}

