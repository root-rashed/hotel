package com.example.app.repository;

import com.example.app.model.entity.Payment;
import com.example.app.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByBookingId(Long bookingId);

    Optional<Payment> findByTransactionReference(String transactionReference);

    List<Payment> findByPaymentStatus(PaymentStatus status);
}
