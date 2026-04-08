package com.edulearn.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create-order")
    @Operation(summary = "Create payment order", description = "Create a Razorpay order for course purchase")
    public ResponseEntity<Map<String, String>> createOrder(@RequestBody Map<String, Object> request) {
        Integer studentId = ((Number) request.get("studentId")).intValue();
        Integer courseId = ((Number) request.get("courseId")).intValue();
        Double amount = ((Number) request.get("amount")).doubleValue();

        Map<String, String> response = paymentService.createOrder(studentId, courseId, amount);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify payment", description = "Verify Razorpay payment signature and mark as SUCCESS")
    public ResponseEntity<Payment> verifyPayment(@RequestBody Map<String, String> request) {
        String razorpayOrderId = request.get("razorpayOrderId");
        String razorpayPaymentId = request.get("razorpayPaymentId");
        String razorpaySignature = request.get("razorpaySignature");
        Integer studentId = Integer.parseInt(request.get("studentId"));
        Integer courseId = Integer.parseInt(request.get("courseId"));

        Payment payment = paymentService.verifyPayment(razorpayOrderId, razorpayPaymentId,
                razorpaySignature, studentId, courseId);

        return ResponseEntity.ok(payment);
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get payment history", description = "Get all payments for a student ordered by date")
    public ResponseEntity<List<Payment>> getPaymentHistory(@PathVariable Integer studentId) {
        List<Payment> payments = paymentService.getPaymentHistory(studentId);
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/refund/{paymentId}")
    @Operation(summary = "Refund payment", description = "Refund a payment (admin only)")
    public ResponseEntity<Payment> refundPayment(@PathVariable Integer paymentId) {
        Payment refundedPayment = paymentService.refundPayment(paymentId);
        return ResponseEntity.ok(refundedPayment);
    }

    @PostMapping("/subscriptions/subscribe")
    @Operation(summary = "Subscribe to plan", description = "Subscribe to FREE, MONTHLY, or ANNUAL plan")
    public ResponseEntity<Subscription> subscribe(@RequestBody Map<String, Object> request) {
        Integer studentId = ((Number) request.get("studentId")).intValue();
        String plan = (String) request.get("plan");

        Subscription subscription = paymentService.subscribe(studentId, plan);
        return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
    }

    @DeleteMapping("/subscriptions/cancel/{studentId}")
    @Operation(summary = "Cancel subscription", description = "Cancel active subscription for a student")
    public ResponseEntity<Void> cancelSubscription(@PathVariable Integer studentId) {
        paymentService.cancelSubscription(studentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/subscriptions/status/{studentId}")
    @Operation(summary = "Check subscription status", description = "Check if student has active subscription")
    public ResponseEntity<Boolean> getSubscriptionStatus(@PathVariable Integer studentId) {
        boolean isActive = paymentService.isSubscriptionActive(studentId);
        return ResponseEntity.ok(isActive);
    }

    @GetMapping("/subscriptions/student/{studentId}")
    @Operation(summary = "Get subscription details", description = "Get subscription details for a student")
    public ResponseEntity<?> getSubscriptionDetails(@PathVariable Integer studentId) {
        java.util.Optional<Subscription> subscription = paymentService.getSubscriptionByStudent(studentId);
        if (subscription.isPresent()) {
            return ResponseEntity.ok(subscription.get());
        }
        return ResponseEntity.notFound().build();
    }
}

