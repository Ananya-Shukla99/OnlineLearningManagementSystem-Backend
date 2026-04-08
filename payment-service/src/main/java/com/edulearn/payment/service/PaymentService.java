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

    Map<String, String> createOrder(Integer studentId, Integer courseId, Double amount);

    Payment verifyPayment(String razorpayOrderId, String razorpayPaymentId,
                         String razorpaySignature, Integer studentId, Integer courseId);

    List<Payment> getPaymentsByStudent(Integer studentId);

    List<Payment> getPaymentHistory(Integer studentId);

    Subscription subscribe(Integer studentId, String plan);

    void cancelSubscription(Integer studentId);

    boolean isSubscriptionActive(Integer studentId);

    Payment refundPayment(Integer paymentId);

    Optional<Subscription> getSubscriptionByStudent(Integer studentId);
}

