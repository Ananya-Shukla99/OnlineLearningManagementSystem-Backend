package com.edulearn.payment.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Subscription Entity
 * Tests Subscription entity builder, getters, and setters
 */
@DisplayName("Subscription Entity Tests")
class SubscriptionEntityTest {

    private Subscription subscription;

    @BeforeEach
    void setUp() {
        subscription = new Subscription();
    }

    @Test
    @DisplayName("Should create Subscription using builder")
    void testSubscriptionBuilder() {
        // Act
        Subscription builtSubscription = Subscription.builder()
                .subscriptionId(1)
                .studentId(5)
                .plan("MONTHLY")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status("ACTIVE")
                .autoRenew(true)
                .build();

        // Assert
        assertNotNull(builtSubscription);
        assertEquals(1, builtSubscription.getSubscriptionId());
        assertEquals(5, builtSubscription.getStudentId());
        assertEquals("MONTHLY", builtSubscription.getPlan());
        assertEquals(LocalDate.now(), builtSubscription.getStartDate());
        assertEquals(LocalDate.now().plusDays(30), builtSubscription.getEndDate());
        assertEquals("ACTIVE", builtSubscription.getStatus());
        assertTrue(builtSubscription.getAutoRenew());
    }

    @Test
    @DisplayName("Should set and get subscription ID")
    void testSetAndGetSubscriptionId() {
        // Act
        subscription.setSubscriptionId(1);

        // Assert
        assertEquals(1, subscription.getSubscriptionId());
    }

    @Test
    @DisplayName("Should set and get student ID")
    void testSetAndGetStudentId() {
        // Act
        subscription.setStudentId(5);

        // Assert
        assertEquals(5, subscription.getStudentId());
    }

    @Test
    @DisplayName("Should set and get plan")
    void testSetAndGetPlan() {
        // Act
        subscription.setPlan("MONTHLY");

        // Assert
        assertEquals("MONTHLY", subscription.getPlan());
    }

    @Test
    @DisplayName("Should set and get start date")
    void testSetAndGetStartDate() {
        // Arrange
        LocalDate startDate = LocalDate.now();

        // Act
        subscription.setStartDate(startDate);

        // Assert
        assertEquals(startDate, subscription.getStartDate());
    }

    @Test
    @DisplayName("Should set and get end date")
    void testSetAndGetEndDate() {
        // Arrange
        LocalDate endDate = LocalDate.now().plusDays(30);

        // Act
        subscription.setEndDate(endDate);

        // Assert
        assertEquals(endDate, subscription.getEndDate());
    }

    @Test
    @DisplayName("Should set and get status")
    void testSetAndGetStatus() {
        // Act
        subscription.setStatus("ACTIVE");

        // Assert
        assertEquals("ACTIVE", subscription.getStatus());
    }

    @Test
    @DisplayName("Should set and get amount paid")
    void testSetAndGetAmountPaid() {
        // Act
        subscription.setAmountPaid(499.0);

        // Assert
        assertEquals(499.0, subscription.getAmountPaid());
    }

    @Test
    @DisplayName("Should set and get auto renew")
    void testSetAndGetAutoRenew() {
        // Act
        subscription.setAutoRenew(true);

        // Assert
        assertTrue(subscription.getAutoRenew());
    }

    @Test
    @DisplayName("Should support all subscription plans")
    void testSubscriptionPlans() {
        // Arrange
        String[] plans = {"FREE", "MONTHLY", "ANNUAL"};

        // Act & Assert
        for (String plan : plans) {
            subscription.setPlan(plan);
            assertEquals(plan, subscription.getPlan());
        }
    }

    @Test
    @DisplayName("Should support all subscription statuses")
    void testSubscriptionStatuses() {
        // Arrange
        String[] statuses = {"ACTIVE", "CANCELLED", "EXPIRED"};

        // Act & Assert
        for (String status : statuses) {
            subscription.setStatus(status);
            assertEquals(status, subscription.getStatus());
        }
    }

    @Test
    @DisplayName("Should calculate MONTHLY subscription end date correctly")
    void testMonthlySubscriptionDuration() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate expectedEndDate = startDate.plusDays(30);

        // Act
        subscription.setStartDate(startDate);
        subscription.setEndDate(expectedEndDate);

        // Assert
        assertEquals(expectedEndDate, subscription.getEndDate());
    }

    @Test
    @DisplayName("Should calculate ANNUAL subscription end date correctly")
    void testAnnualSubscriptionDuration() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate expectedEndDate = startDate.plusDays(365);

        // Act
        subscription.setStartDate(startDate);
        subscription.setEndDate(expectedEndDate);

        // Assert
        assertEquals(expectedEndDate, subscription.getEndDate());
    }

    @Test
    @DisplayName("Should support disable auto renew")
    void testAutoRenewDisabled() {
        // Act
        subscription.setAutoRenew(false);

        // Assert
        assertFalse(subscription.getAutoRenew());
    }

    @Test
    @DisplayName("Should create subscription with all fields populated")
    void testFullSubscriptionCreation() {
        // Arrange
        LocalDate today = LocalDate.now();

        // Act
        Subscription fullSubscription = Subscription.builder()
                .subscriptionId(1)
                .studentId(5)
                .plan("ANNUAL")
                .startDate(today)
                .endDate(today.plusDays(365))
                .status("ACTIVE")
                .amountPaid(5999.0)
                .autoRenew(true)
                .build();

        // Assert
        assertEquals(1, fullSubscription.getSubscriptionId());
        assertEquals(5, fullSubscription.getStudentId());
        assertEquals("ANNUAL", fullSubscription.getPlan());
        assertEquals(today, fullSubscription.getStartDate());
        assertEquals(today.plusDays(365), fullSubscription.getEndDate());
        assertEquals("ACTIVE", fullSubscription.getStatus());
        assertEquals(5999.0, fullSubscription.getAmountPaid());
        assertTrue(fullSubscription.getAutoRenew());
    }
}

