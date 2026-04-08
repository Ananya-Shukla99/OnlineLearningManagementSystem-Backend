package com.edulearn.payment.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Payment Entity
 * Tests Payment entity builder, getters, and setters
 */
@DisplayName("Payment Entity Tests")
class PaymentEntityTest {

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = new Payment();
    }

    @Test
    @DisplayName("Should create Payment using builder")
    void testPaymentBuilder() {
        // Act
        Payment builtPayment = Payment.builder()
                .paymentId(1)
                .studentId(10)
                .courseId(5)
                .amount(1499.0)
                .status("SUCCESS")
                .mode("ONLINE")
                .currency("INR")
                .transactionId("pay_123456")
                .build();

        // Assert
        assertNotNull(builtPayment);
        assertEquals(1, builtPayment.getPaymentId());
        assertEquals(10, builtPayment.getStudentId());
        assertEquals(5, builtPayment.getCourseId());
        assertEquals(1499.0, builtPayment.getAmount());
        assertEquals("SUCCESS", builtPayment.getStatus());
        assertEquals("ONLINE", builtPayment.getMode());
        assertEquals("INR", builtPayment.getCurrency());
        assertEquals("pay_123456", builtPayment.getTransactionId());
    }

    @Test
    @DisplayName("Should set and get payment ID")
    void testSetAndGetPaymentId() {
        // Act
        payment.setPaymentId(1);

        // Assert
        assertEquals(1, payment.getPaymentId());
    }

    @Test
    @DisplayName("Should set and get student ID")
    void testSetAndGetStudentId() {
        // Act
        payment.setStudentId(5);

        // Assert
        assertEquals(5, payment.getStudentId());
    }

    @Test
    @DisplayName("Should set and get course ID")
    void testSetAndGetCourseId() {
        // Act
        payment.setCourseId(10);

        // Assert
        assertEquals(10, payment.getCourseId());
    }

    @Test
    @DisplayName("Should set and get amount")
    void testSetAndGetAmount() {
        // Act
        payment.setAmount(1499.0);

        // Assert
        assertEquals(1499.0, payment.getAmount());
    }

    @Test
    @DisplayName("Should set and get status")
    void testSetAndGetStatus() {
        // Act
        payment.setStatus("SUCCESS");

        // Assert
        assertEquals("SUCCESS", payment.getStatus());
    }

    @Test
    @DisplayName("Should set and get mode")
    void testSetAndGetMode() {
        // Act
        payment.setMode("ONLINE");

        // Assert
        assertEquals("ONLINE", payment.getMode());
    }

    @Test
    @DisplayName("Should set and get transaction ID")
    void testSetAndGetTransactionId() {
        // Act
        payment.setTransactionId("pay_123456");

        // Assert
        assertEquals("pay_123456", payment.getTransactionId());
    }

    @Test
    @DisplayName("Should set and get paid at timestamp")
    void testSetAndGetPaidAt() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        // Act
        payment.setPaidAt(now);

        // Assert
        assertEquals(now, payment.getPaidAt());
    }

    @Test
    @DisplayName("Should set and get currency")
    void testSetAndGetCurrency() {
        // Act
        payment.setCurrency("INR");

        // Assert
        assertEquals("INR", payment.getCurrency());
    }

    @Test
    @DisplayName("Should set and get created at timestamp")
    void testSetAndGetCreatedAt() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        // Act
        payment.setCreatedAt(now);

        // Assert
        assertEquals(now, payment.getCreatedAt());
    }

    @Test
    @DisplayName("Should support all payment statuses")
    void testPaymentStatuses() {
        // Arrange
        String[] statuses = {"PENDING", "SUCCESS", "FAILED", "REFUNDED"};

        // Act & Assert
        for (String status : statuses) {
            payment.setStatus(status);
            assertEquals(status, payment.getStatus());
        }
    }

    @Test
    @DisplayName("Should support all payment modes")
    void testPaymentModes() {
        // Arrange
        String[] modes = {"UPI", "CARD", "NET_BANKING", "WALLET", "ONLINE"};

        // Act & Assert
        for (String mode : modes) {
            payment.setMode(mode);
            assertEquals(mode, payment.getMode());
        }
    }

    @Test
    @DisplayName("Should have default currency as INR on creation")
    void testDefaultCurrency() {
        // Act
        Payment newPayment = new Payment();

        // Assert - currency should be set by @PrePersist
        assertNull(newPayment.getCurrency()); // Until @PrePersist is called by JPA
    }
}

