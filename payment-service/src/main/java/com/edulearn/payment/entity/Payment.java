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
    private Integer paymentId;

    @Column(nullable = false)
    private Integer studentId;

    @Column(nullable = false)
    private Integer courseId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String status; // "PENDING", "SUCCESS", "FAILED", "REFUNDED"

    @Column(nullable = false)
    private String mode; // "UPI", "CARD", "NET_BANKING", "WALLET"

    @Column(name = "transaction_id")
    private String transactionId; // Razorpay payment ID

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
    }

    // Explicit getters and setters
    public Integer getPaymentId() { return paymentId; }
    public void setPaymentId(Integer paymentId) { this.paymentId = paymentId; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

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
        private Integer paymentId;
        private Integer studentId;
        private Integer courseId;
        private Double amount;
        private String status;
        private String mode;
        private String transactionId;
        private LocalDateTime paidAt;
        private String currency;
        private LocalDateTime createdAt;

        public PaymentBuilder paymentId(Integer paymentId) { this.paymentId = paymentId; return this; }
        public PaymentBuilder studentId(Integer studentId) { this.studentId = studentId; return this; }
        public PaymentBuilder courseId(Integer courseId) { this.courseId = courseId; return this; }
        public PaymentBuilder amount(Double amount) { this.amount = amount; return this; }
        public PaymentBuilder status(String status) { this.status = status; return this; }
        public PaymentBuilder mode(String mode) { this.mode = mode; return this; }
        public PaymentBuilder transactionId(String transactionId) { this.transactionId = transactionId; return this; }
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
            payment.paidAt = this.paidAt;
            payment.currency = this.currency;
            payment.createdAt = this.createdAt;
            return payment;
        }
    }
}

