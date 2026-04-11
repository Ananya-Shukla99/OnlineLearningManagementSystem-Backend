package com.edulearn.payment.service;

import com.edulearn.payment.entity.Payment;
import com.edulearn.payment.entity.Subscription;
import com.edulearn.payment.repository.PaymentRepository;
import com.edulearn.payment.repository.SubscriptionRepository;
import com.edulearn.notification.event.PaymentEvent;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementation of PaymentService
 * Handles payment processing with Razorpay integration
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Value("${enrollment.service.url:http://localhost:8082/api/v1}")
    private String enrollmentServiceUrl;

    private RazorpayClient razorpayClient;


     //Initialize Razorpay client
    @Autowired
    public void initRazorpay() {
        try {
            this.razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Razorpay client: " + e.getMessage());
        }
    }

    /**
     * Create a payment order with Razorpay
     * Returns orderId to be used in frontend checkout
     */
    @Override
    public Map<String, String> createOrder(Integer studentId, Integer courseId, Double amount) {
        try {
            long amountInPaise = (long) (amount * 100);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "receipt_" + studentId + "_" + courseId);

             com.razorpay.Order order = razorpayClient.orders.create(orderRequest);

             // Create Payment record with PENDING status
             Payment payment = new Payment();
             payment.setStudentId(studentId);
             payment.setCourseId(courseId);
             payment.setAmount(amount);
             payment.setStatus("PENDING");
             payment.setMode("ONLINE");
             payment.setCurrency("INR");

             paymentRepository.save(payment);

            Map<String, String> response = new HashMap<>();
            response.put("orderId", order.get("id"));
            response.put("currency", "INR");
            response.put("amount", String.valueOf(amountInPaise));

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Razorpay order: " + e.getMessage());
        }
    }

    /**
     * Verify Razorpay payment signature
     * Mark payment as SUCCESS and automatically enroll student in course
     */
    @Override
    @Transactional
    public Payment verifyPayment(String razorpayOrderId, String razorpayPaymentId,
                                 String razorpaySignature, Integer studentId, Integer courseId) {
        try {
            // Step 1: Create signature string
            String signatureData = razorpayOrderId + "|" + razorpayPaymentId;

            // Step 2: Compute HMAC-SHA256
            String computedSignature = generateSignature(signatureData, razorpayKeySecret);

            // Step 3: Compare signatures
            if (!computedSignature.equals(razorpaySignature)) {
                throw new RuntimeException("Payment verification failed: Signature mismatch");
            }

            // Step 4: Find payment record
            Optional<Payment> paymentOpt = paymentRepository
                    .findByStudentIdOrderByPaidAtDesc(studentId)
                    .stream()
                    .findFirst();

            if (paymentOpt.isEmpty()) {
                throw new RuntimeException("Payment record not found");
            }

            Payment payment = paymentOpt.get();

            // Step 5: Update payment status
            payment.setStatus("SUCCESS");
            payment.setTransactionId(razorpayPaymentId);
            payment.setPaidAt(LocalDateTime.now());

            Payment savedPayment = paymentRepository.save(payment);

            // Step 6: Call enrollment-service via HTTP to enroll student
            try {
                enrollStudentViaHttp(studentId, courseId);
            } catch (Exception e) {
                // Log error but don't fail the payment - payment is already successful
                System.err.println("Warning: Could not enroll student via HTTP: " + e.getMessage());
            }

            // Publish payment event - NotificationServiceImpl will listen and create notification
            eventPublisher.publishEvent(
                    new PaymentEvent(this, studentId, payment.getAmount(), "Course " + courseId)
            );

            return savedPayment;
        } catch (Exception e) {
            throw new RuntimeException("Payment verification error: " + e.getMessage());
        }
    }

    /**
     * Calls enrollment-service HTTP endpoint to enroll student
     * This works when enrollment-service is running on a separate port
     */
    private void enrollStudentViaHttp(Integer studentId, Integer courseId) {
        try {
            String url = enrollmentServiceUrl + "/enrollments/enroll";

            Map<String, Integer> enrollmentRequest = new HashMap<>();
            enrollmentRequest.put("studentId", studentId);
            enrollmentRequest.put("courseId", courseId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Integer>> request = new HttpEntity<>(enrollmentRequest, headers);

            restTemplate.postForObject(url, request, Object.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to enroll student: " + e.getMessage());
        }
    }

    /**
     * Generate HMAC-SHA256 signature
     */
    private String generateSignature(String data, String key) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(
                key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    @Override
    public List<Payment> getPaymentsByStudent(Integer studentId) {
        return paymentRepository.findByStudentId(studentId);
    }

    @Override
    public List<Payment> getPaymentHistory(Integer studentId) {
        return paymentRepository.findByStudentIdOrderByPaidAtDesc(studentId);
    }

    @Override
    @Transactional
    public Subscription subscribe(Integer studentId, String plan) {
        // Check if student already has active subscription
        Optional<Subscription> existingSub = subscriptionRepository
                .findByStudentIdAndStatus(studentId, "ACTIVE");

        if (existingSub.isPresent()) {
            throw new RuntimeException("Already have an active subscription");
        }

        LocalDate startDate = LocalDate.now();
        LocalDate endDate;

        // Calculate end date based on plan
        switch (plan.toUpperCase()) {
            case "FREE":
                endDate = startDate.plusDays(36500); // Effectively never expires
                break;
            case "MONTHLY":
                endDate = startDate.plusDays(30);
                break;
            case "ANNUAL":
                endDate = startDate.plusDays(365);
                 break;
             default:
                 throw new RuntimeException("Invalid subscription plan: " + plan);
         }

         Subscription subscription = new Subscription();
         subscription.setStudentId(studentId);
         subscription.setPlan(plan);
         subscription.setStartDate(startDate);
         subscription.setEndDate(endDate);
         subscription.setStatus("ACTIVE");
         subscription.setAutoRenew(false);

         return subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public void cancelSubscription(Integer studentId) {
        Optional<Subscription> subOpt = subscriptionRepository
                .findByStudentIdAndStatus(studentId, "ACTIVE");

        if (subOpt.isPresent()) {
            Subscription subscription = subOpt.get();
            subscription.setStatus("CANCELLED");
            subscriptionRepository.save(subscription);
        } else {
            throw new RuntimeException("No active subscription found");
        }
    }

    @Override
    public boolean isSubscriptionActive(Integer studentId) {
        Optional<Subscription> subOpt = subscriptionRepository
                .findByStudentIdAndStatus(studentId, "ACTIVE");

        if (subOpt.isEmpty()) {
            return false;
        }

        Subscription subscription = subOpt.get();
        return subscription.getEndDate().isAfter(LocalDate.now());
    }

    @Override
    @Transactional
    public Payment refundPayment(Integer paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus("REFUNDED");
        return paymentRepository.save(payment);
    }

    @Override
    public Optional<Subscription> getSubscriptionByStudent(Integer studentId) {
        return subscriptionRepository.findByStudentIdAndStatus(studentId, "ACTIVE");
    }
}

