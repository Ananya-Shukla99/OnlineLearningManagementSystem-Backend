package com.edulearn.course.controller;

import com.edulearn.course.entity.Course;
import com.edulearn.course.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/courses")
@Tag(name = "Courses", description = "Course Management API endpoints")
public class CourseController {

    @Autowired
    private CourseService courseService;

    /**
     * GET /courses
     * Get all published courses (no auth needed)
     */
    @GetMapping
    @Operation(summary = "Get all published courses", description = "Retrieve all published courses available for students")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved courses")
    })
    public ResponseEntity<Map<String, Object>> getAllCourses() {
        try {
            List<Course> courses = courseService.getAllCourses();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Courses retrieved successfully");
            response.put("data", courses);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * GET /courses/{id}
     * Get single course details (no auth needed)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get course by ID", description = "Retrieve details of a specific course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<Map<String, Object>> getCourseById(@PathVariable Integer id) {
        try {
            Optional<Course> course = courseService.getCourseById(id);

            if (course.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Course not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Course retrieved successfully");
            response.put("data", course.get());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * GET /courses/search?keyword=X
     * Search courses by keyword (no auth needed)
     */
    @GetMapping("/search")
    @Operation(summary = "Search courses", description = "Search courses by keyword in title and description")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    public ResponseEntity<Map<String, Object>> searchCourses(@RequestParam String keyword) {
        try {
            List<Course> courses = courseService.searchCourses(keyword);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Search completed successfully");
            response.put("data", courses);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * GET /courses/category/{category}
     * Filter by category (no auth needed)
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Get courses by category", description = "Retrieve all courses in a specific category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Courses retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getCoursesByCategory(@PathVariable String category) {
        try {
            List<Course> courses = courseService.getCoursesByCategory(category);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Courses retrieved successfully");
            response.put("data", courses);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * GET /courses/featured
     * Get top 6 featured courses (no auth needed)
     */
    @GetMapping("/featured")
    @Operation(summary = "Get featured courses", description = "Retrieve top 6 featured courses")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Featured courses retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getFeaturedCourses() {
        try {
            List<Course> courses = courseService.getFeaturedCourses();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Featured courses retrieved successfully");
            response.put("data", courses);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * GET /courses/instructor/{instructorId}
     * Get courses by instructor (no auth needed)
     */
    @GetMapping("/instructor/{instructorId}")
    @Operation(summary = "Get courses by instructor", description = "Retrieve all courses created by a specific instructor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Courses retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getCoursesByInstructor(@PathVariable Integer instructorId) {
        try {
            List<Course> courses = courseService.getCoursesByInstructor(instructorId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Courses retrieved successfully");
            response.put("data", courses);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * POST /courses
     * Create course (INSTRUCTOR only)
     */
    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Create a new course", description = "Create a new course (INSTRUCTOR only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Course created successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - not an instructor")
    })
    public ResponseEntity<Map<String, Object>> createCourse(@RequestBody Course course) {
        try {
            Course createdCourse = courseService.createCourse(course);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Course created successfully");
            response.put("data", createdCourse);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * PUT /courses/{id}
     * Update course (INSTRUCTOR only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Update a course", description = "Update course details (INSTRUCTOR only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course updated successfully"),
            @ApiResponse(responseCode = "404", description = "Course not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - not an instructor")
    })
    public ResponseEntity<Map<String, Object>> updateCourse(@PathVariable Integer id, @RequestBody Course course) {
        try {
            Course updatedCourse = courseService.updateCourse(id, course);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Course updated successfully");
            response.put("data", updatedCourse);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * PUT /courses/{id}/publish
     * Publish course (INSTRUCTOR only)
     */
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Submit course for review", description = "Submit a course for admin review (INSTRUCTOR only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course published successfully"),
            @ApiResponse(responseCode = "404", description = "Course not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - not an instructor")
    })
    public ResponseEntity<Map<String, Object>> publishCourse(@PathVariable Integer id) {
        try {
            courseService.publishCourse(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Course submitted for review");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> approveCourse(@PathVariable Integer id) {
        try {
            courseService.approveCourse(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Course approved and published"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rejectCourse(@PathVariable Integer id,
            @RequestParam(defaultValue = "Rejected by admin review") String reason) {
        try {
            courseService.rejectCourse(id, reason);
            return ResponseEntity.ok(Map.of("success", true, "message", "Course rejected"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPendingCourses() {
        try {
            return ResponseEntity.ok(Map.of("success", true, "data", courseService.getPendingCourses()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * DELETE /courses/{id}
     * Delete course (INSTRUCTOR only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Delete a course", description = "Delete a course (INSTRUCTOR or ADMIN)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Course not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - not an instructor")
    })
    public ResponseEntity<Map<String, Object>> deleteCourse(@PathVariable Integer id) {
        try {
            courseService.deleteCourse(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Course deleted successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * GET /courses/all
     * Get all courses including unpublished (ADMIN only)
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all courses", description = "Retrieve all courses including unpublished (ADMIN only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All courses retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - not an admin")
    })
    public ResponseEntity<Map<String, Object>> getAllCoursesAdmin() {
        try {
            List<Course> courses = courseService.getAllCoursesIncludingUnpublished();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "All courses retrieved successfully");
            response.put("data", courses);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
