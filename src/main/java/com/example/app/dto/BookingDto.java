package com.example.app.dto;

import com.example.app.model.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Read-side representation of a Booking, flattened for the view layer
 * to avoid exposing entity graphs (and prevent Customer <-> Booking
 * bidirectional serialization loops) directly to Thymeleaf/JSON.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDto {

    private Long id;
    private String bookingReference;

    private Long customerId;
    private String customerName;

    private Long roomId;
    private String roomNumber;
    private String roomTypeName;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfGuests;
    private BigDecimal totalAmount;
    private BookingStatus bookingStatus;
    private LocalDateTime createdAt;
}
