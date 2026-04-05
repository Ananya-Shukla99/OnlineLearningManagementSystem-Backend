package com.edulearn.enrollment.controller;

import com.edulearn.enrollment.entity.Enrollment;
import com.edulearn.enrollment.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/enrollments")
@CrossOrigin(origins = "*")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    // POST /enrollments/enroll (STUDENT only)
    @PostMapping("/enroll")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<Map<String, Object>> enroll(@RequestBody Map<String, Object> request) {
        Long studentId = ((Number) request.get("studentId")).longValue();
        Integer courseId = ((Number) request.get("courseId")).intValue();

        Enrollment enrollment = enrollmentService.enroll(studentId, courseId);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("success", true, "message", "Successfully enrolled in course", "data", enrollment)
        );
    }

    // DELETE /enrollments/{enrollmentId} (STUDENT only)
    @DeleteMapping("/{enrollmentId}")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<Map<String, Object>> unenroll(@PathVariable Long enrollmentId) {
        enrollmentService.unenroll(enrollmentId);

        return ResponseEntity.ok(
                Map.of("success", true, "message", "Successfully unenrolled from course")
        );
    }

    // GET /enrollments/student/{studentId}
    @GetMapping("/student/{studentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getEnrollmentsByStudent(@PathVariable Long studentId) {
        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByStudent(studentId);

        return ResponseEntity.ok(
                Map.of("success", true, "data", enrollments)
        );
    }

    // GET /enrollments/course/{courseId} (INSTRUCTOR/ADMIN only)
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyAuthority('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getEnrollmentsByCourse(@PathVariable Integer courseId) {
        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByCourse(courseId);

        return ResponseEntity.ok(
                Map.of("success", true, "data", enrollments)
        );
    }

    // PUT /enrollments/progress
    @PutMapping("/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> updateProgress(@RequestBody Map<String, Object> request) {
        Long studentId = ((Number) request.get("studentId")).longValue();
        Integer courseId = ((Number) request.get("courseId")).intValue();
        Integer progressPercent = ((Number) request.get("progressPercent")).intValue();

        enrollmentService.updateProgress(studentId, courseId, progressPercent);

        return ResponseEntity.ok(
                Map.of("success", true, "message", "Progress updated successfully")
        );
    }

    // PUT /enrollments/complete/{enrollmentId} (INSTRUCTOR/ADMIN only)
    @PutMapping("/complete/{enrollmentId}")
    @PreAuthorize("hasAnyAuthority('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> markComplete(@PathVariable Long enrollmentId) {
        enrollmentService.markComplete(enrollmentId);

        return ResponseEntity.ok(
                Map.of("success", true, "message", "Enrollment marked as completed")
        );
    }

    // GET /enrollments/check?studentId=X&courseId=Y
    @GetMapping("/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> isEnrolled(
            @RequestParam Long studentId,
            @RequestParam Integer courseId) {

        boolean enrolled = enrollmentService.isEnrolled(studentId, courseId);

        return ResponseEntity.ok(
                Map.of("success", true, "data", enrolled)
        );
    }

    // GET /enrollments/count/{courseId}
    @GetMapping("/count/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getEnrollmentCount(@PathVariable Integer courseId) {
        int count = enrollmentService.getEnrollmentCount(courseId);

        return ResponseEntity.ok(
                Map.of("success", true, "data", count)
        );
    }
}

