package com.edulearn.progress.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
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
    public ResponseEntity<Void> trackProgress(@RequestBody Map<String, Object> request) {
        Long studentId = ((Number) request.get("studentId")).longValue();
        Long courseId = ((Number) request.get("courseId")).longValue();
        Long lessonId = ((Number) request.get("lessonId")).longValue();
        Integer watchedSeconds = (Integer) request.get("watchedSeconds");

        progressService.trackProgress(studentId, courseId, lessonId, watchedSeconds);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/complete")
    @Operation(summary = "Mark lesson complete", description = "Mark lesson as 100% complete")
    public ResponseEntity<Map<String, Object>> markLessonComplete(@RequestBody Map<String, Object> request) {
        Long studentId = ((Number) request.get("studentId")).longValue();
        Long courseId = ((Number) request.get("courseId")).longValue();
        Long lessonId = ((Number) request.get("lessonId")).longValue();

        Map<String, Object> result = progressService.markLessonComplete(studentId, courseId, lessonId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/course")
    @Operation(summary = "Get course progress", description = "Get course completion percentage (0-100)")
    public ResponseEntity<Integer> getCourseProgress(@RequestParam Long studentId, @RequestParam Long courseId) {
        Integer progress = progressService.getCourseProgress(studentId, courseId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/lesson")
    @Operation(summary = "Get lesson progress", description = "Get progress details for a specific lesson")
    public ResponseEntity<?> getLessonProgress(@RequestParam Long studentId, @RequestParam Long lessonId) {
        Optional<Progress> progress = progressService.getLessonProgress(studentId, lessonId);
        if (progress.isPresent()) {
            return ResponseEntity.ok(progress.get());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/all/{studentId}")
    @Operation(summary = "Get all progress", description = "Get all progress records for a student")
    public ResponseEntity<List<Progress>> getAllProgressByStudent(@PathVariable Long studentId) {
        List<Progress> progressList = progressService.getAllProgressByStudent(studentId);
        return ResponseEntity.ok(progressList);
    }

    @PostMapping("/certificates/issue")
    @Operation(summary = "Issue certificate", description = "Manually trigger certificate generation")
    public ResponseEntity<Map<String, Object>> issueCertificate(@RequestParam Long studentId, @RequestParam Long courseId) {
        Certificate certificate = progressService.issueCertificate(studentId, courseId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", certificate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/certificates/student/{studentId}")
    @Operation(summary = "Get student certificates", description = "Get all certificates issued to a student")
    public ResponseEntity<Map<String, Object>> getStudentCertificates(@PathVariable Long studentId) {
        System.out.println("=====================================================");
        System.out.println("DIAGNOSTIC: Fetching certificates for student ID: " + studentId);
        List<Certificate> certificates = progressService.getAllCertificatesByStudent(studentId);
        System.out.println("DIAGNOSTIC: Database returned " + (certificates != null ? certificates.size() : 0) + " certificates.");
        if (certificates != null && certificates.size() > 0) {
            certificates.forEach(c -> System.out.println("   - Cert ID: " + c.getCertificateId() + " for Course: " + c.getCourseName()));
        }
        System.out.println("=====================================================");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", certificates);
        return ResponseEntity.ok(response);
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

    @GetMapping("/certificates/download/{filename}")
    @Operation(summary = "Download certificate", description = "Download certificate PDF file")
    public ResponseEntity<org.springframework.core.io.Resource> downloadCertificate(@PathVariable String filename) {
        try {
            java.nio.file.Path path = java.nio.file.Paths.get("src/main/resources/certificates/").resolve(filename).normalize();
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(path.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                        .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

