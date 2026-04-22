package com.edulearn.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.edulearn.payment.entity.Payment;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Payment entity
 * Handles all database operations for payments
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByStudentId(Long studentId);

    List<Payment> findByCourseId(Long courseId);

    List<Payment> findByStatus(String status);

    Optional<Payment> findByTransactionId(String transactionId);

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    List<Payment> findByStudentIdOrderByPaidAtDesc(Long studentId);
}

