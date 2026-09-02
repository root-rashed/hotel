package com.example.app.exception;

/**
 * Raised for duplicate room number, duplicate username/email, duplicate
 * identification number, etc. Kept separate from BookingException since
 * it applies across multiple modules, not just bookings.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
