package com.edulearn.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Feign Client for inter-service communication with enrollment-service.
 * Spring Cloud resolves "enrollment-service" via Eureka automatically —
 * no hardcoded URLs needed.
 */
@FeignClient(name = "enrollment-service")
public interface EnrollmentClient {

    @PostMapping("/api/v1/enrollments/enroll")
    Map<String, Object> enroll(@RequestBody Map<String, Object> request);
}
