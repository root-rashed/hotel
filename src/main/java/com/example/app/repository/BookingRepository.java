package com.example.app.repository;

import com.example.app.model.entity.Booking;
import com.example.app.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);

    List<Booking> findByCustomerId(Long customerId);

    List<Booking> findByRoomId(Long roomId);

    List<Booking> findByBookingStatus(BookingStatus status);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.room.id = :roomId
            AND b.bookingStatus <> com.example.app.model.enums.BookingStatus.CANCELLED
            AND b.checkInDate < :checkOutDate
            AND b.checkOutDate > :checkInDate
            """)
    List<Booking> findOverlappingBookings(@Param("roomId") Long roomId,
                                           @Param("checkInDate") LocalDate checkInDate,
                                           @Param("checkOutDate") LocalDate checkOutDate);

    List<Booking> findByCheckInDate(LocalDate date);

    List<Booking> findByCheckOutDate(LocalDate date);

    List<Booking> findByCheckInDateBetween(LocalDate start, LocalDate end);
}
