package com.example.app.service;

import com.example.app.dto.PaymentDto;
import com.example.app.exception.BookingException;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.model.entity.Booking;
import com.example.app.model.entity.Payment;
import com.example.app.model.enums.PaymentStatus;
import com.example.app.repository.BookingRepository;
import com.example.app.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Service
@Transactional
public class PaymentService {

    private static final String REF_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    public PaymentService(PaymentRepository paymentRepository, BookingRepository bookingRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public List<PaymentDto> findAll() {
        return paymentRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentDto> findByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public PaymentDto findById(Long id) {
        return toDto(getEntity(id));
    }

    public PaymentDto record(PaymentDto dto) {
        Booking booking = bookingRepository.findById(dto.getBookingId())
                .orElseThrow(() -> ResourceNotFoundException.of("Booking", dto.getBookingId()));

        if (dto.getAmount() == null || dto.getAmount().signum() <= 0) {
            throw new BookingException("Payment amount must be positive.");
        }

        Payment payment = Payment.builder()
                .booking(booking)
                .amount(dto.getAmount())
                .paymentMethod(dto.getPaymentMethod())
                .paymentStatus(PaymentStatus.PAID)
                .transactionReference(generateTransactionReference())
                .build();

        return toDto(paymentRepository.save(payment));
    }

    private Payment getEntity(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Payment", id));
    }

    private String generateTransactionReference() {
        String candidate;
        do {
            StringBuilder sb = new StringBuilder("TXN");
            for (int i = 0; i < 10; i++) {
                sb.append(REF_ALPHABET.charAt(RANDOM.nextInt(REF_ALPHABET.length())));
            }
            candidate = sb.toString();
        } while (paymentRepository.findByTransactionReference(candidate).isPresent());
        return candidate;
    }

    private PaymentDto toDto(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .bookingId(payment.getBooking().getId())
                .bookingReference(payment.getBooking().getBookingReference())
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .transactionReference(payment.getTransactionReference())
                .build();
    }
}