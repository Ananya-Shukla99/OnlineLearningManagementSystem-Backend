package com.edulearn.payment.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import com.edulearn.payment.entity.Subscription;

/**
 * Integration tests for SubscriptionRepository
 * Tests subscription database operations using H2 in-memory database
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create"
})
@Sql({"/schema.sql"})
@DisplayName("SubscriptionRepository Integration Tests")
class SubscriptionRepositoryTest {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private Subscription testSubscription;

    @BeforeEach
    void setUp() {
        try {
            subscriptionRepository.deleteAll();
        } catch (Exception e) {
            // Database may not be initialized yet
        }

        testSubscription = Subscription.builder()
                .studentId(1)
                .plan("MONTHLY")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status("ACTIVE")
                .autoRenew(false)
                .build();
    }

    @Test
    @DisplayName("Should save subscription successfully")
    void testSaveSubscription() {
        // Act
        Subscription savedSubscription = subscriptionRepository.save(testSubscription);

        // Assert
        assertNotNull(savedSubscription.getSubscriptionId());
        assertEquals("MONTHLY", savedSubscription.getPlan());
        assertEquals("ACTIVE", savedSubscription.getStatus());
    }

    @Test
    @DisplayName("Should find subscription by ID")
    void testFindSubscriptionById() {
        // Arrange
        Subscription savedSubscription = subscriptionRepository.save(testSubscription);

        // Act
        Optional<Subscription> foundSubscription = subscriptionRepository.findById(savedSubscription.getSubscriptionId());

        // Assert
        assertTrue(foundSubscription.isPresent());
        assertEquals(savedSubscription.getSubscriptionId(), foundSubscription.get().getSubscriptionId());
    }

    @Test
    @DisplayName("Should find active subscription by student ID and status")
    void testFindByStudentIdAndStatus() {
        // Arrange
        subscriptionRepository.save(testSubscription);

        // Act
        Optional<Subscription> foundSubscription = subscriptionRepository.findByStudentIdAndStatus(1, "ACTIVE");

        // Assert
        assertTrue(foundSubscription.isPresent());
        assertEquals("ACTIVE", foundSubscription.get().getStatus());
    }

    @Test
    @DisplayName("Should not find inactive subscription")
    void testFindByStudentIdAndStatusNotFound() {
        // Arrange
        subscriptionRepository.save(testSubscription);

        // Act
        Optional<Subscription> foundSubscription = subscriptionRepository.findByStudentIdAndStatus(1, "CANCELLED");

        // Assert
        assertTrue(foundSubscription.isEmpty());
    }

    @Test
    @DisplayName("Should update subscription status")
    void testUpdateSubscriptionStatus() {
        // Arrange
        Subscription savedSubscription = subscriptionRepository.save(testSubscription);

        // Act
        savedSubscription.setStatus("CANCELLED");
        Subscription updatedSubscription = subscriptionRepository.save(savedSubscription);

        // Assert
        assertEquals("CANCELLED", updatedSubscription.getStatus());
    }

    @Test
    @DisplayName("Should delete subscription by ID")
    void testDeleteSubscriptionById() {
        // Arrange
        Subscription savedSubscription = subscriptionRepository.save(testSubscription);

        // Act
        subscriptionRepository.deleteById(savedSubscription.getSubscriptionId());

        // Assert
        Optional<Subscription> foundSubscription = subscriptionRepository.findById(savedSubscription.getSubscriptionId());
        assertTrue(foundSubscription.isEmpty());
    }

    @Test
    @DisplayName("Should find MONTHLY subscription with correct end date")
    void testMonthlySubscriptionEndDate() {
        // Act
        Subscription savedSubscription = subscriptionRepository.save(testSubscription);

        // Assert
        assertEquals(LocalDate.now().plusDays(30), savedSubscription.getEndDate());
    }

}

