package com.example.app.controller;

import com.example.app.dto.BookingDto;
import com.example.app.dto.BookingRequestDto;
import com.example.app.dto.CustomerDto;
import com.example.app.exception.UnauthorizedException;
import com.example.app.model.enums.BookingStatus;
import com.example.app.service.BookingService;
import com.example.app.service.CustomerService;
import com.example.app.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    private final CustomerService customerService;
    private final BookingService bookingService;
    private final RoomService roomService;

    public CustomerController(CustomerService customerService, BookingService bookingService, RoomService roomService) {
        this.customerService = customerService;
        this.bookingService = bookingService;
        this.roomService = roomService;
    }


    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        List<BookingDto> myBookings = bookingService.findByCustomerUsername(authentication.getName());

        LocalDate today = LocalDate.now();
        model.addAttribute("currentBooking", myBookings.stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.CHECKED_IN)
                .findFirst().orElse(null));
        model.addAttribute("upcomingBookings", myBookings.stream()
                .filter(b -> b.getBookingStatus() != BookingStatus.CANCELLED
                        && b.getBookingStatus() != BookingStatus.CHECKED_OUT
                        && !b.getCheckInDate().isBefore(today))
                .toList());
        model.addAttribute("previousBookings", myBookings.stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.CHECKED_OUT)
                .toList());
        model.addAttribute("profile", customerService.findByUsername(authentication.getName()));
        return "customer/dashboard";
    }

    @GetMapping("/rooms")
    public String browseRooms(@RequestParam(required = false) String checkIn,
                               @RequestParam(required = false) String checkOut,
                               Model model) {
        if (checkIn != null && checkOut != null && !checkIn.isBlank() && !checkOut.isBlank()) {
            model.addAttribute("rooms", roomService.findAvailableRooms(LocalDate.parse(checkIn), LocalDate.parse(checkOut)));
        } else {
            model.addAttribute("rooms", roomService.findAll());
        }
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);
        return "customer/rooms";
    }

    @GetMapping("/rooms/{id}")
    public String roomDetails(@PathVariable Long id, Model model) {
        model.addAttribute("room", roomService.findById(id));
        return "customer/room-details";
    }

    @GetMapping("/bookings/new")
    public String newBookingForm(@RequestParam(required = false) Long roomId, Model model) {
        model.addAttribute("bookingRequestDto", BookingRequestDto.builder().roomId(roomId).build());
        return "customer/booking-form";
    }

    @PostMapping("/bookings")
    public String createBooking(@Valid @ModelAttribute("bookingRequestDto") BookingRequestDto dto,
                                 BindingResult bindingResult, Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return "customer/booking-form";
        }
        bookingService.createBySelf(dto, authentication.getName());
        return "redirect:/customer/bookings";
    }

    @GetMapping("/bookings")
    public String myBookings(Authentication authentication, Model model) {
        model.addAttribute("bookings", bookingService.findByCustomerUsername(authentication.getName()));
        return "customer/bookings";
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, Authentication authentication) {
        // Ownership is enforced inside BookingService.cancel() by comparing
        // the authenticated username's customer profile against the booking.
        bookingService.cancel(id, authentication.getName());
        return "redirect:/customer/bookings";
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        model.addAttribute("customerDto", customerService.findByUsername(authentication.getName()));
        return "customer/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("customerDto") CustomerDto dto,
                                 BindingResult bindingResult, Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return "customer/profile";
        }
        CustomerDto existing = customerService.findByUsername(authentication.getName());
        if (!existing.getId().equals(dto.getId())) {
            throw new UnauthorizedException("You can only edit your own profile.");
        }
        customerService.update(dto.getId(), dto);
        return "redirect:/customer/profile";
    }
}
