package com.example.app.model.enums;

/**
 * Operational status of a physical room. This is a coarse, current-state flag —
 * date-based availability for booking is calculated separately from Booking
 * records, never derived solely from this field (see BookingService).
 */
public enum RoomStatus {
    AVAILABLE,
    OCCUPIED,
    RESERVED,
    MAINTENANCE
}
