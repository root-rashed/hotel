package com.example.app.dto;

import com.example.app.model.enums.PaymentMethod;
import com.example.app.model.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDto {

    private Long id;

    @NotNull
    private Long bookingId;

    private String bookingReference;

    @NotNull
    @Positive
    private BigDecimal amount;

    private LocalDateTime paymentDate;

    @NotNull
    private PaymentMethod paymentMethod;

    private PaymentStatus paymentStatus;

    private String transactionReference;
}
