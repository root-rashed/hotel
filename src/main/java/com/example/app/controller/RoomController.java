package com.example.app.controller;

import com.example.app.dto.RoomDto;
import com.example.app.service.RoomService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Read-only room lookup shared across roles. Room creation/editing/deletion
 * is ADMIN-only and lives in AdminController under /admin/rooms.
 */
@Controller
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("rooms", roomService.findAll());
        return "customer/rooms";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        RoomDto room = roomService.findById(id);
        model.addAttribute("room", room);
        return "customer/room-details";
    }
}
