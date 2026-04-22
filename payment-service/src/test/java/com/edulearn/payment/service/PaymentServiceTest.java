package com.edulearn.payment.service;

import com.edulearn.payment.entity.Payment;
import com.edulearn.payment.entity.Subscription;
import com.edulearn.payment.repository.PaymentRepository;
import com.edulearn.payment.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentService
 * Tests payment and subscription operations using Mockito
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Unit Tests")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;


    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment testPayment;
    private Subscription testSubscription;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testPayment = Payment.builder()
                .paymentId(1L)
                .studentId(1L)
                .courseId(5L)
                .amount(1499.0)
                .status("SUCCESS")
                .mode("ONLINE")
                .currency("INR")
                .transactionId("pay_123456")
                .build();

        testSubscription = Subscription.builder()
                .subscriptionId(1L)
                .studentId(1L)
                .plan("MONTHLY")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status("ACTIVE")
                .autoRenew(false)
                .build();
    }

    // ==================== SUBSCRIPTION TESTS ====================

    @Test
    @DisplayName("Should create MONTHLY subscription successfully")
    void testSubscribeMonthlySuccess() {
        // Arrange
        Long studentId = 1L;
        String plan = "MONTHLY";

        when(subscriptionRepository.findByStudentIdAndStatus(studentId, "ACTIVE"))
                .thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class)))
                .thenReturn(testSubscription);

        // Act
        Subscription result = paymentService.subscribe(studentId, plan);

        // Assert
        assertNotNull(result);
        assertEquals("MONTHLY", result.getPlan());
        assertEquals("ACTIVE", result.getStatus());
        assertEquals(LocalDate.now().plusDays(30), result.getEndDate());
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Should create ANNUAL subscription with correct duration")
    void testSubscribeAnnualSuccess() {
        // Arrange
        Long studentId = 2L;
        String plan = "ANNUAL";

        Subscription annualSubscription = Subscription.builder()
                .subscriptionId(2L)
                .studentId(2L)
                .plan("ANNUAL")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(365))
                .status("ACTIVE")
                .autoRenew(false)
                .build();

        when(subscriptionRepository.findByStudentIdAndStatus(studentId, "ACTIVE"))
                .thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class)))
                .thenReturn(annualSubscription);

        // Act
        Subscription result = paymentService.subscribe(studentId, plan);

        // Assert
        assertNotNull(result);
        assertEquals("ANNUAL", result.getPlan());
        assertEquals(LocalDate.now().plusDays(365), result.getEndDate());
    }

    @Test
    @DisplayName("Should create FREE subscription successfully")
    void testSubscribeFreeSuccess() {
        // Arrange
        Long studentId = 3L;
        String plan = "FREE";
        Subscription freeSub = Subscription.builder()
                .studentId(3L)
                .plan("FREE")
                .status("ACTIVE")
                .endDate(LocalDate.now().plusDays(36500))
                .build();

        when(subscriptionRepository.findByStudentIdAndStatus(studentId, "ACTIVE"))
                .thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class)))
                .thenReturn(freeSub);

        // Act
        Subscription result = paymentService.subscribe(studentId, plan);

        // Assert
        assertEquals("FREE", result.getPlan());
    }

    @Test
    @DisplayName("Should throw exception for unknown plan")
    void testSubscribeInvalidPlan() {
        // Arrange
        Long studentId = 1L;
        String plan = "INVALID";

        when(subscriptionRepository.findByStudentIdAndStatus(studentId, "ACTIVE"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> paymentService.subscribe(studentId, plan));
    }

    @Test
    @DisplayName("Should throw exception when student already has active subscription")
    void testSubscribeThrowsExceptionWhenAlreadyActive() {
        // Arrange
        Long studentId = 1L;
        String plan = "MONTHLY";

        when(subscriptionRepository.findByStudentIdAndStatus(studentId, "ACTIVE"))
                .thenReturn(Optional.of(testSubscription));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                paymentService.subscribe(studentId, plan),
                "Should throw exception for duplicate subscription"
        );
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Should throw exception when canceling non-existent subscription")
    void testCancelSubscriptionNotFound() {
        // Arrange
        Long studentId = 1L;
        when(subscriptionRepository.findByStudentIdAndStatus(studentId, "ACTIVE"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> paymentService.cancelSubscription(studentId));
    }

    @Test
    @DisplayName("Should cancel active subscription successfully")
    void testCancelSubscriptionSuccess() {
        // Arrange
        Long studentId = 1L;
        Subscription activeSubscription = Subscription.builder()
                .subscriptionId(1L)
                .studentId(1L)
                .plan("MONTHLY")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status("ACTIVE")
                .autoRenew(false)
                .build();

        when(subscriptionRepository.findByStudentIdAndStatus(studentId, "ACTIVE"))
                .thenReturn(Optional.of(activeSubscription));

        // Act
        paymentService.cancelSubscription(studentId);

        // Assert
        verify(subscriptionRepository, times(1)).save(argThat(sub ->
                sub.getStatus().equals("CANCELLED")
        ));
    }

    @Test
    @DisplayName("Should return true when subscription is active and not expired")
    void testIsSubscriptionActiveTrueWhenActive() {
        // Arrange
        Long studentId = 1L;
        Subscription activeSubscription = Subscription.builder()
                .subscriptionId(1L)
                .studentId(1L)
                .plan("MONTHLY")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(15))
                .status("ACTIVE")
                .autoRenew(false)
                .build();

        when(subscriptionRepository.findByStudentIdAndStatus(studentId, "ACTIVE"))
                .thenReturn(Optional.of(activeSubscription));

        // Act
        boolean result = paymentService.isSubscriptionActive(studentId);

        // Assert
        assertTrue(result, "Subscription should be active");
    }

    @Test
    @DisplayName("Should return false when subscription has expired")
    void testIsSubscriptionActiveFalseWhenExpired() {
        // Arrange
        Long studentId = 1L;
        Subscription expiredSubscription = Subscription.builder()
                .subscriptionId(1L)
                .studentId(1L)
                .plan("MONTHLY")
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now().minusDays(1))
                .status("ACTIVE")
                .autoRenew(false)
                .build();

        when(subscriptionRepository.findByStudentIdAndStatus(studentId, "ACTIVE"))
                .thenReturn(Optional.of(expiredSubscription));

        // Act
        boolean result = paymentService.isSubscriptionActive(studentId);

        // Assert
        assertFalse(result, "Subscription should not be active when expired");
    }

    @Test
    @DisplayName("Should return false when no subscription exists")
    void testIsSubscriptionActiveFalseWhenNone() {
        // Arrange
        Long studentId = 1L;

        when(subscriptionRepository.findByStudentIdAndStatus(studentId, "ACTIVE"))
                .thenReturn(Optional.empty());

        // Act
        boolean result = paymentService.isSubscriptionActive(studentId);

        // Assert
        assertFalse(result, "Should return false when no subscription");
    }

    // ==================== PAYMENT HISTORY TESTS ====================

    @Test
    @DisplayName("Should get all payments for a student")
    void testGetPaymentsByStudent() {
        // Arrange
        Long studentId = 1L;
        List<Payment> payments = List.of(testPayment);

        when(paymentRepository.findByStudentId(studentId))
                .thenReturn(payments);

        // Act
        List<Payment> result = paymentService.getPaymentsByStudent(studentId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(studentId, result.get(0).getStudentId());
        verify(paymentRepository, times(1)).findByStudentId(studentId);
    }

    @Test
    @DisplayName("Should get payment history sorted by date")
    void testGetPaymentHistory() {
        // Arrange
        Long studentId = 1L;
        List<Payment> payments = List.of(testPayment);

        when(paymentRepository.findByStudentIdOrderByPaidAtDesc(studentId))
                .thenReturn(payments);

        // Act
        List<Payment> result = paymentService.getPaymentHistory(studentId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentRepository, times(1)).findByStudentIdOrderByPaidAtDesc(studentId);
    }

    // ==================== REFUND TESTS ====================

    @Test
    @DisplayName("Should refund payment successfully")
    void testRefundPaymentSuccess() {
        // Arrange
        Long paymentId = 1L;
        Payment payment = testPayment.builder()
                .paymentId(paymentId)
                .status("SUCCESS")
                .build();
        Payment refundedPayment = payment.builder()
                .status("REFUNDED")
                .build();

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(refundedPayment);

        // Act
        Payment result = paymentService.refundPayment(paymentId);

        // Assert
        assertNotNull(result);
        assertEquals("REFUNDED", result.getStatus());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw exception when payment not found for refund")
    void testRefundPaymentThrowsExceptionWhenNotFound() {
        // Arrange
        Long paymentId = 999L;

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                paymentService.refundPayment(paymentId)
        );
    }

    // ==================== GET SUBSCRIPTION TESTS ====================

    @Test
    @DisplayName("Should get active subscription for student")
    void testGetSubscriptionByStudent() {
        // Arrange
        Long studentId = 1L;

        when(subscriptionRepository.findByStudentIdAndStatus(studentId, "ACTIVE"))
                .thenReturn(Optional.of(testSubscription));

        // Act
        Optional<Subscription> result = paymentService.getSubscriptionByStudent(studentId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("MONTHLY", result.get().getPlan());
        assertEquals("ACTIVE", result.get().getStatus());
    }

    @Test
    @DisplayName("Should return empty optional when no active subscription")
    void testGetSubscriptionByStudentReturnsEmptyWhenNone() {
        // Arrange
        Long studentId = 1L;

        when(subscriptionRepository.findByStudentIdAndStatus(studentId, "ACTIVE"))
                .thenReturn(Optional.empty());

        // Act
        Optional<Subscription> result = paymentService.getSubscriptionByStudent(studentId);

        // Assert
        assertTrue(result.isEmpty());
    }
}

