package com.example.app.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Write-side payload for creating a booking. Deliberately narrow — it
 * carries only what the client submits; the service resolves the actual
 * Customer/Room entities, computes totalAmount, and sets initial status.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequestDto {

    // Only set by staff creating a booking on behalf of a customer; when a
    // CUSTOMER submits this themselves, the service resolves it from the
    // authenticated principal instead and ignores/overrides this value.
    private Long customerId;

    @NotNull
    private Long roomId;

    @NotNull
    @FutureOrPresent
    private LocalDate checkInDate;

    @NotNull
    @Future
    private LocalDate checkOutDate;

    @NotNull
    @Positive
    private Integer numberOfGuests;
}
