package com.example.app.controller;

import com.example.app.dto.BookingRequestDto;
import com.example.app.dto.CustomerDto;
import com.example.app.model.enums.BookingStatus;
import com.example.app.service.BookingService;
import com.example.app.service.CustomerService;
import com.example.app.service.PaymentService;
import com.example.app.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/reception")
public class ReceptionController {

    private final CustomerService customerService;
    private final RoomService roomService;
    private final BookingService bookingService;
    private final PaymentService paymentService;

    public ReceptionController(CustomerService customerService, RoomService roomService,
                                BookingService bookingService, PaymentService paymentService) {
        this.customerService = customerService;
        this.roomService = roomService;
        this.bookingService = bookingService;
        this.paymentService = paymentService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        var bookings = bookingService.findAll();
        LocalDate today = LocalDate.now();

        model.addAttribute("todaysCheckIns", bookings.stream()
                .filter(b -> b.getCheckInDate().isEqual(today) && b.getBookingStatus() != BookingStatus.CANCELLED)
                .toList());
        model.addAttribute("todaysCheckOuts", bookings.stream()
                .filter(b -> b.getCheckOutDate().isEqual(today) && b.getBookingStatus() == BookingStatus.CHECKED_IN)
                .toList());
        model.addAttribute("availableRoomsCount", roomService.findAll().stream()
                .filter(r -> r.getStatus().name().equals("AVAILABLE")).count());
        model.addAttribute("pendingBookings", bookings.stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.PENDING).toList());
        model.addAttribute("recentCustomers", customerService.findAll().stream().limit(5).toList());
        return "reception/dashboard";
    }

    // ---------- Customers ----------

    @GetMapping("/customers")
    public String customers(Model model) {
        model.addAttribute("customers", customerService.findAll());
        return "reception/customers";
    }

    @GetMapping("/customers/new")
    public String newCustomerForm(Model model) {
        model.addAttribute("customerDto", CustomerDto.builder().build());
        return "reception/customer-form";
    }

    @GetMapping("/customers/{id}/edit")
    public String editCustomerForm(@PathVariable Long id, Model model) {
        model.addAttribute("customerDto", customerService.findById(id));
        return "reception/customer-form";
    }

    @PostMapping("/customers")
    public String saveCustomer(@Valid @ModelAttribute("customerDto") CustomerDto dto,
                                BindingResult bindingResult,
                                @RequestParam(required = false) String password) {
        if (bindingResult.hasErrors()) {
            return "reception/customer-form";
        }
        if (dto.getId() == null) {
            customerService.create(dto, password);
        } else {
            customerService.update(dto.getId(), dto);
        }
        return "redirect:/reception/customers";
    }

    // ---------- Bookings ----------

    @GetMapping("/bookings")
    public String bookings(Model model) {
        model.addAttribute("bookings", bookingService.findAll());
        return "reception/bookings";
    }

    @GetMapping("/bookings/new")
    public String newBookingForm(Model model) {
        model.addAttribute("bookingRequestDto", BookingRequestDto.builder().build());
        model.addAttribute("customers", customerService.findAll());
        model.addAttribute("rooms", roomService.findAll());
        return "reception/booking-form";
    }

    @PostMapping("/bookings")
    public String createBooking(@Valid @ModelAttribute("bookingRequestDto") BookingRequestDto dto,
                                 BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("customers", customerService.findAll());
            model.addAttribute("rooms", roomService.findAll());
            return "reception/booking-form";
        }
        bookingService.createByStaff(dto);
        return "redirect:/reception/bookings";
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable Long id) {
        bookingService.cancel(id, null);
        return "redirect:/reception/bookings";
    }

    // ---------- Check-in / Check-out ----------

    @GetMapping("/check-in")
    public String checkInPage(Model model) {
        model.addAttribute("bookings", bookingService.findAll().stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.PENDING || b.getBookingStatus() == BookingStatus.CONFIRMED)
                .toList());
        return "reception/check-in";
    }

    @PostMapping("/check-in/{id}")
    public String checkIn(@PathVariable Long id) {
        bookingService.checkIn(id);
        return "redirect:/reception/check-in";
    }

    @GetMapping("/check-out")
    public String checkOutPage(Model model) {
        model.addAttribute("bookings", bookingService.findAll().stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.CHECKED_IN)
                .toList());
        return "reception/check-out";
    }

    @PostMapping("/check-out/{id}")
    public String checkOut(@PathVariable Long id) {
        bookingService.checkOut(id);
        return "redirect:/reception/check-out";
    }

    // ---------- Payments ----------

    @GetMapping("/payments")
    public String payments(Model model) {
        model.addAttribute("payments", paymentService.findAll());
        model.addAttribute("bookings", bookingService.findAll());
        return "reception/payments";
    }
}
