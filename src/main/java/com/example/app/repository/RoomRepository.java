package com.example.app.repository;

import com.example.app.model.entity.Room;
import com.example.app.model.enums.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByRoomNumber(String roomNumber);

    boolean existsByRoomNumber(String roomNumber);

    List<Room> findByStatus(RoomStatus status);

    List<Room> findByRoomTypeId(Long roomTypeId);

    // Rooms that have NO overlapping, non-cancelled booking for the requested
    // date range. Overlap test: existingCheckIn < requestedCheckOut AND
    // existingCheckOut > requestedCheckIn.
    @Query("""
            SELECT r FROM Room r
            WHERE r.status <> com.example.app.model.enums.RoomStatus.MAINTENANCE
            AND r.id NOT IN (
                SELECT b.room.id FROM Booking b
                WHERE b.bookingStatus <> com.example.app.model.enums.BookingStatus.CANCELLED
                AND b.checkInDate < :checkOutDate
                AND b.checkOutDate > :checkInDate
            )
            """)
    List<Room> findAvailableRooms(@Param("checkInDate") LocalDate checkInDate,
                                   @Param("checkOutDate") LocalDate checkOutDate);
}
