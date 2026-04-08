package com.edulearn.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Subscription entity for subscription plans
 * Allows students to subscribe for unlimited course access
 */
@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer subscriptionId;

    @Column(nullable = false)
    private Integer studentId;

    @Column(nullable = false)
    private String plan; // "FREE", "MONTHLY", "ANNUAL"

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private String status; // "ACTIVE", "CANCELLED", "EXPIRED"

    @Column(name = "amount_paid")
    private Double amountPaid;

    @Column(name = "auto_renew", nullable = false)
    private Boolean autoRenew;

    @PrePersist
    protected void onCreate() {
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (autoRenew == null) {
            autoRenew = false;
        }
        if (status == null) {
            status = "ACTIVE";
        }
    }

    // Explicit getters and setters
    public Integer getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(Integer subscriptionId) { this.subscriptionId = subscriptionId; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(Double amountPaid) { this.amountPaid = amountPaid; }

    public Boolean getAutoRenew() { return autoRenew; }
    public void setAutoRenew(Boolean autoRenew) { this.autoRenew = autoRenew; }

    // Builder method for fluent object creation
    public static SubscriptionBuilder builder() {
        return new SubscriptionBuilder();
    }

    public static class SubscriptionBuilder {
        private Integer subscriptionId;
        private Integer studentId;
        private String plan;
        private LocalDate startDate;
        private LocalDate endDate;
        private String status;
        private Double amountPaid;
        private Boolean autoRenew;

        public SubscriptionBuilder subscriptionId(Integer subscriptionId) { this.subscriptionId = subscriptionId; return this; }
        public SubscriptionBuilder studentId(Integer studentId) { this.studentId = studentId; return this; }
        public SubscriptionBuilder plan(String plan) { this.plan = plan; return this; }
        public SubscriptionBuilder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public SubscriptionBuilder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
        public SubscriptionBuilder status(String status) { this.status = status; return this; }
        public SubscriptionBuilder amountPaid(Double amountPaid) { this.amountPaid = amountPaid; return this; }
        public SubscriptionBuilder autoRenew(Boolean autoRenew) { this.autoRenew = autoRenew; return this; }

        public Subscription build() {
            Subscription subscription = new Subscription();
            subscription.subscriptionId = this.subscriptionId;
            subscription.studentId = this.studentId;
            subscription.plan = this.plan;
            subscription.startDate = this.startDate;
            subscription.endDate = this.endDate;
            subscription.status = this.status;
            subscription.amountPaid = this.amountPaid;
            subscription.autoRenew = this.autoRenew;
            return subscription;
        }
    }
}

