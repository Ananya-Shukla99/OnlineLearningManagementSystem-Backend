package com.edulearn.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Payment entity to track all course purchases
 * Records Razorpay transaction details and links students to courses
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String status; // "PENDING", "SUCCESS", "FAILED", "REFUNDED"

    @Column(nullable = false)
    private String mode; // "UPI", "CARD", "NET_BANKING", "WALLET"

    @Column(name = "transaction_id")
    private String transactionId; // Razorpay transaction ref

    @Column(name = "razorpay_order_id")
    private String razorpayOrderId; 

    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;

    @Column(name = "razorpay_signature")
    private String razorpaySignature;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(nullable = false)
    private String currency; // "INR" by default

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (currency == null) {
            currency = "INR";
        }
        if (status == null) {
            status = "PENDING";
        }
        if (mode == null) {
            mode = "ONLINE";
        }
    }

    // Explicit getters and setters
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }

    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }

    public String getRazorpaySignature() { return razorpaySignature; }
    public void setRazorpaySignature(String razorpaySignature) { this.razorpaySignature = razorpaySignature; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Builder method for fluent object creation
    public static PaymentBuilder builder() {
        return new PaymentBuilder();
    }

    public static class PaymentBuilder {
        private Long paymentId;
        private Long studentId;
        private Long courseId;
        private Double amount;
        private String status;
        private String mode;
        private String transactionId;
        private String razorpayOrderId;
        private String razorpayPaymentId;
        private String razorpaySignature;
        private LocalDateTime paidAt;
        private String currency;
        private LocalDateTime createdAt;

        public PaymentBuilder paymentId(Long paymentId) { this.paymentId = paymentId; return this; }
        public PaymentBuilder studentId(Long studentId) { this.studentId = studentId; return this; }
        public PaymentBuilder courseId(Long courseId) { this.courseId = courseId; return this; }
        public PaymentBuilder amount(Double amount) { this.amount = amount; return this; }
        public PaymentBuilder status(String status) { this.status = status; return this; }
        public PaymentBuilder mode(String mode) { this.mode = mode; return this; }
        public PaymentBuilder transactionId(String transactionId) { this.transactionId = transactionId; return this; }
        public PaymentBuilder razorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; return this; }
        public PaymentBuilder razorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; return this; }
        public PaymentBuilder razorpaySignature(String razorpaySignature) { this.razorpaySignature = razorpaySignature; return this; }
        public PaymentBuilder paidAt(LocalDateTime paidAt) { this.paidAt = paidAt; return this; }
        public PaymentBuilder currency(String currency) { this.currency = currency; return this; }
        public PaymentBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Payment build() {
            Payment payment = new Payment();
            payment.paymentId = this.paymentId;
            payment.studentId = this.studentId;
            payment.courseId = this.courseId;
            payment.amount = this.amount;
            payment.status = this.status;
            payment.mode = this.mode;
            payment.transactionId = this.transactionId;
            payment.razorpayOrderId = this.razorpayOrderId;
            payment.razorpayPaymentId = this.razorpayPaymentId;
            payment.razorpaySignature = this.razorpaySignature;
            payment.paidAt = this.paidAt;
            payment.currency = this.currency;
            payment.createdAt = this.createdAt;
            return payment;
        }
    }
}

