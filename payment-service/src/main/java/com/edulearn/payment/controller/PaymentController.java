package com.edulearn.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.edulearn.payment.dto.VerifyPaymentRequest;
import com.edulearn.payment.entity.Payment;
import com.edulearn.payment.entity.Subscription;
import com.edulearn.payment.service.PaymentService;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Payment operations
 * Handles payment creation, verification, and subscription management
 */
@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment Service", description = "Payment processing and subscription management")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-order")
    @Operation(summary = "Create payment order", description = "Create a Razorpay order for course purchase")
    public ResponseEntity<Map<String, String>> createOrder(@RequestBody Map<String, Object> request) {
        Long studentId = ((Number) request.get("studentId")).longValue();
        Long courseId = ((Number) request.get("courseId")).longValue();
        Double amount = ((Number) request.get("amount")).doubleValue();

        try {
            Map<String, String> response = paymentService.createOrder(studentId, courseId, amount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Print stack trace internally so it appears in the terminal logs
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("error", "Internal Server Error: " + e.getMessage()));
        }
    }

    @GetMapping("/razorpay-key")
    @Operation(summary = "Get Razorpay Key", description = "Get public Razorpay Key ID")
    public ResponseEntity<Map<String, String>> getRazorpayKey() {
        return ResponseEntity.ok(Map.of("keyId", paymentService.getRazorpayKeyId()));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify payment", description = "Verify Razorpay payment signature and mark as SUCCESS")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, Object> request) {
        try {
            if (request.get("studentId") == null || request.get("courseId") == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "studentId and courseId are required"));
            }

            Payment payment = paymentService.verifyPayment(
                    request.get("razorpayOrderId") != null ? (String) request.get("razorpayOrderId") : "",
                    request.get("razorpayPaymentId") != null ? (String) request.get("razorpayPaymentId") : "",
                    request.get("razorpaySignature") != null ? (String) request.get("razorpaySignature") : "",
                    Long.parseLong(request.get("studentId").toString()),
                    Long.parseLong(request.get("courseId").toString())
            );

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "paymentId", payment.getPaymentId(),
                    "razorpayOrderId", payment.getRazorpayOrderId(),
                    "studentId", payment.getStudentId()
            ));
        } catch (Throwable t) {
            System.err.println("CRITICAL VERIFICATION ERROR OCCURRED!");
            t.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", t.toString()));
        }
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get payment history", description = "Get all payments for a student ordered by date")
    public ResponseEntity<List<Payment>> getPaymentHistory(@PathVariable Long studentId) {
        List<Payment> payments = paymentService.getPaymentHistory(studentId);
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/refund/{paymentId}")
    @Operation(summary = "Refund payment", description = "Refund a payment (admin only)")
    public ResponseEntity<Payment> refundPayment(@PathVariable Long paymentId) {
        Payment refundedPayment = paymentService.refundPayment(paymentId);
        return ResponseEntity.ok(refundedPayment);
    }

    @PostMapping("/subscriptions/subscribe")
    @Operation(summary = "Subscribe to plan", description = "Subscribe to FREE, MONTHLY, or ANNUAL plan")
    public ResponseEntity<Subscription> subscribe(@RequestBody Map<String, Object> request) {
        Long studentId = ((Number) request.get("studentId")).longValue();
        String plan = (String) request.get("plan");

        Subscription subscription = paymentService.subscribe(studentId, plan);
        return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
    }

    @DeleteMapping("/subscriptions/cancel/{studentId}")
    @Operation(summary = "Cancel subscription", description = "Cancel active subscription for a student")
    public ResponseEntity<Void> cancelSubscription(@PathVariable Long studentId) {
        paymentService.cancelSubscription(studentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/subscriptions/status/{studentId}")
    @Operation(summary = "Check subscription status", description = "Check if student has active subscription")
    public ResponseEntity<Boolean> getSubscriptionStatus(@PathVariable Long studentId) {
        boolean isActive = paymentService.isSubscriptionActive(studentId);
        return ResponseEntity.ok(isActive);
    }

    @GetMapping("/subscriptions/student/{studentId}")
    @Operation(summary = "Get subscription details", description = "Get subscription details for a student")
    public ResponseEntity<?> getSubscriptionDetails(@PathVariable Long studentId) {
        java.util.Optional<Subscription> subscription = paymentService.getSubscriptionByStudent(studentId);
        if (subscription.isPresent()) {
            return ResponseEntity.ok(subscription.get());
        }
        return ResponseEntity.notFound().build();
    }
}

