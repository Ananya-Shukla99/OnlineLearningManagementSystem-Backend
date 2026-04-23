package com.edulearn.payment.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import com.edulearn.payment.entity.Payment;

/**
 * Integration tests for PaymentRepository
 * Tests database operations using H2 in-memory database
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create"
})
@Sql({"/schema.sql"})
@DisplayName("PaymentRepository Integration Tests")
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment testPayment;

    @BeforeEach
    void setUp() {
        try {
            paymentRepository.deleteAll();
        } catch (Exception e) {
            // Database may not be initialized yet
        }

        testPayment = Payment.builder()
                .studentId(1L)
                .courseId(5L)
                .amount(1499.0)
                .status("SUCCESS")
                .mode("ONLINE")
                .currency("INR")
                .transactionId("pay_123456")
                .paidAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should save payment successfully")
    void testSavePayment() {
        // Act
        Payment savedPayment = paymentRepository.save(testPayment);

        // Assert
        assertNotNull(savedPayment.getPaymentId());
        assertEquals("SUCCESS", savedPayment.getStatus());
        assertEquals(1L, savedPayment.getStudentId());
    }

    @Test
    @DisplayName("Should find payment by ID")
    void testFindPaymentById() {
        // Arrange
        Payment savedPayment = paymentRepository.save(testPayment);

        // Act
        Optional<Payment> foundPayment = paymentRepository.findById(savedPayment.getPaymentId());

        // Assert
        assertTrue(foundPayment.isPresent());
        assertEquals(savedPayment.getPaymentId(), foundPayment.get().getPaymentId());
    }

    @Test
    @DisplayName("Should find all payments by student ID")
    void testFindByStudentId() {
        // Arrange
        paymentRepository.save(testPayment);

        // Act
        List<Payment> payments = paymentRepository.findByStudentId(1L);

        // Assert
        assertEquals(1, payments.size());
        assertEquals(1L, payments.get(0).getStudentId());
    }

    @Test
    @DisplayName("Should find payment by transaction ID")
    void testFindByTransactionId() {
        // Arrange
        Payment savedPayment = paymentRepository.save(testPayment);

        // Act
        Optional<Payment> foundPayment = paymentRepository.findByTransactionId("pay_123456");

        // Assert
        assertTrue(foundPayment.isPresent());
        assertEquals(savedPayment.getPaymentId(), foundPayment.get().getPaymentId());
    }

    @Test
    @DisplayName("Should update payment status")
    void testUpdatePaymentStatus() {
        // Arrange
        Payment savedPayment = paymentRepository.save(testPayment);

        // Act
        savedPayment.setStatus("REFUNDED");
        Payment updatedPayment = paymentRepository.save(savedPayment);

        // Assert
        assertEquals("REFUNDED", updatedPayment.getStatus());
    }

    @Test
    @DisplayName("Should delete payment by ID")
    void testDeletePaymentById() {
        // Arrange
        Payment savedPayment = paymentRepository.save(testPayment);

        // Act
        paymentRepository.deleteById(savedPayment.getPaymentId());

        // Assert
        Optional<Payment> foundPayment = paymentRepository.findById(savedPayment.getPaymentId());
        assertTrue(foundPayment.isEmpty());
    }
}

