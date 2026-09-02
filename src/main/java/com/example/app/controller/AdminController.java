package com.example.app.controller;

import com.example.app.dto.BookingDto;
import com.example.app.dto.PaymentDto;
import com.example.app.dto.RoomDto;
import com.example.app.dto.RoomTypeDto;
import com.example.app.dto.UserDto;
import com.example.app.model.enums.BookingStatus;
import com.example.app.model.enums.RoomStatus;
import com.example.app.service.BookingService;
import com.example.app.service.PaymentService;
import com.example.app.service.RoomService;
import com.example.app.service.RoomTypeService;
import com.example.app.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final RoomService roomService;
    private final RoomTypeService roomTypeService;
    private final BookingService bookingService;
    private final PaymentService paymentService;

    public AdminController(UserService userService, RoomService roomService, RoomTypeService roomTypeService,
                            BookingService bookingService, PaymentService paymentService) {
        this.userService = userService;
        this.roomService = roomService;
        this.roomTypeService = roomTypeService;
        this.bookingService = bookingService;
        this.paymentService = paymentService;
    }


    // ---------- Dashboard ----------
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        var rooms = roomService.findAll();
        var bookings = bookingService.findAll();

        long totalRooms = rooms.size();
        long availableRooms = rooms.stream().filter(r -> r.getStatus() == RoomStatus.AVAILABLE).count();
        long occupiedRooms = rooms.stream().filter(r -> r.getStatus() == RoomStatus.OCCUPIED).count();
        BigDecimal revenue = bookings.stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.CHECKED_OUT || b.getBookingStatus() == BookingStatus.CHECKED_IN)
                .map(BookingDto::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("availableRooms", availableRooms);
        model.addAttribute("occupiedRooms", occupiedRooms);
        model.addAttribute("totalBookings", bookings.size());
        model.addAttribute("totalCustomers", userService.findAll().stream()
                .filter(u -> u.getRole().name().equals("CUSTOMER")).count());
        model.addAttribute("revenue", revenue);
        model.addAttribute("recentBookings", bookings.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5).toList());
        return "admin/dashboard";
    }



    // ---------- Users ----------

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users";
    }

    @GetMapping("/users/new")
    public String newUserForm(Model model) {
        model.addAttribute("userDto", UserDto.builder().enabled(true).build());
        return "admin/user-form";
    }

    @GetMapping("/users/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model) {
        model.addAttribute("userDto", userService.findById(id));
        return "admin/user-form";
    }

    @PostMapping("/users")
    public String saveUser(@Valid @ModelAttribute("userDto") UserDto userDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/user-form";
        }
        if (userDto.getId() == null) {
            userService.create(userDto);
        } else {
            userService.update(userDto.getId(), userDto);
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return "redirect:/admin/users";
    }


    // ---------- Room Types ----------
    @GetMapping("/room-types")
    public String roomTypes(Model model) {
        model.addAttribute("roomTypes", roomTypeService.findAll());
        return "admin/room-types";
    }

    @GetMapping("/room-types/new")
    public String newRoomTypeForm(Model model) {
        model.addAttribute("roomTypeDto", RoomTypeDto.builder().build());
        return "admin/room-type-form";
    }

    @GetMapping("/room-types/{id}/edit")
    public String editRoomTypeForm(@PathVariable Long id, Model model) {
        model.addAttribute("roomTypeDto", roomTypeService.findById(id));
        return "admin/room-type-form";
    }

    @PostMapping("/room-types")
    public String saveRoomType(@Valid @ModelAttribute("roomTypeDto") RoomTypeDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/room-type-form";
        }
        if (dto.getId() == null) {
            roomTypeService.create(dto);
        } else {
            roomTypeService.update(dto.getId(), dto);
        }
        return "redirect:/admin/room-types";
    }

    @PostMapping("/room-types/{id}/delete")
    public String deleteRoomType(@PathVariable Long id) {
        roomTypeService.delete(id);
        return "redirect:/admin/room-types";
    }

    // ---------- Rooms ----------

    @GetMapping("/rooms")
    public String rooms(Model model) {
        model.addAttribute("rooms", roomService.findAll());
        return "admin/rooms";
    }

    @GetMapping("/rooms/new")
    public String newRoomForm(Model model) {
        model.addAttribute("roomDto", RoomDto.builder().status(RoomStatus.AVAILABLE).build());
        model.addAttribute("roomTypes", roomTypeService.findAll());
        return "admin/room-form";
    }

    @GetMapping("/rooms/{id}/edit")
    public String editRoomForm(@PathVariable Long id, Model model) {
        model.addAttribute("roomDto", roomService.findById(id));
        model.addAttribute("roomTypes", roomTypeService.findAll());
        return "admin/room-form";
    }

    @PostMapping("/rooms")
    public String saveRoom(@Valid @ModelAttribute("roomDto") RoomDto dto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roomTypes", roomTypeService.findAll());
            return "admin/room-form";
        }
        if (dto.getId() == null) {
            roomService.create(dto);
        } else {
            roomService.update(dto.getId(), dto);
        }
        return "redirect:/admin/rooms";
    }

    @PostMapping("/rooms/{id}/delete")
    public String deleteRoom(@PathVariable Long id) {
        roomService.delete(id);
        return "redirect:/admin/rooms";
    }


    // ---------- Bookings (read/oversight) ----------

    @GetMapping("/bookings")
    public String bookings(Model model) {
        model.addAttribute("bookings", bookingService.findAll());
        return "admin/bookings";
    }

    // ---------- Payments (read/oversight) ----------

    @GetMapping("/payments")
    public String payments(Model model) {
        model.addAttribute("payments", paymentService.findAll());
        return "admin/payments";
    }
}
