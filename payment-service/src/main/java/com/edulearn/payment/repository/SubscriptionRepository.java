package com.edulearn.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.edulearn.payment.entity.Subscription;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Subscription entity
 * Handles all database operations for subscriptions
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
	
    List<Subscription> findByStudentId(Integer studentId);

    Optional<Subscription> findByStudentIdAndStatus(Integer studentId, String status);

    List<Subscription> findByEndDateBefore(LocalDate date);

    long countByPlan(String plan);
}

