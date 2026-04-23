package com.edulearn.payment.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.edulearn.payment.entity.Payment;
import com.edulearn.payment.entity.Subscription;

/**
 * Service interface for Payment operations
 * Defines contract for payment processing with Razorpay
 */
public interface PaymentService {

    Map<String, String> createOrder(Long studentId, Long courseId, Double amount);

    Payment verifyPayment(String razorpayOrderId, String razorpayPaymentId,
                         String razorpaySignature, Long studentId, Long courseId);

    String getRazorpayKeyId();

    List<Payment> getPaymentsByStudent(Long studentId);

    List<Payment> getPaymentHistory(Long studentId);

    Subscription subscribe(Long studentId, String plan);

    void cancelSubscription(Long studentId);

    boolean isSubscriptionActive(Long studentId);

    Payment refundPayment(Long paymentId);

    Optional<Subscription> getSubscriptionByStudent(Long studentId);
}

