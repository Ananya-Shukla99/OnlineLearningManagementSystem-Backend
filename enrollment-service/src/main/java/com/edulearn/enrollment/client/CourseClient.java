package com.edulearn.enrollment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Feign Client for inter-service communication with course-service.
 * Spring Cloud resolves "course-service" via Eureka automatically —
 * no hardcoded URLs needed.
 */
@FeignClient(name = "course-service")
public interface CourseClient {

    @GetMapping("/api/v1/courses/{courseId}")
    Map<String, Object> getCourse(@PathVariable("courseId") Long courseId);
}
