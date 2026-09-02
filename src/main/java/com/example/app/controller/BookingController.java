package com.example.app.controller;

import com.example.app.service.BookingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Shared, role-agnostic booking endpoints reachable by ADMIN, RECEPTIONIST,
 * and CUSTOMER alike (see SecurityConfig's /booking/** rule). Role- and
 * ownership-specific mutations (create/cancel/check-in/check-out) live in
 * AdminController, ReceptionController, and CustomerController, which apply
 * their own scoping before delegating to BookingService.
 */
@Controller
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','CUSTOMER')")
    @GetMapping("/{id}")
    public String viewBooking(@PathVariable Long id, Model model) {
        model.addAttribute("booking", bookingService.findById(id));
        return "reception/bookings";
    }
}
