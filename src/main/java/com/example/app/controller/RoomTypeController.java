package com.example.app.controller;

import com.example.app.service.RoomTypeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Read-only room type lookup shared across roles. Mutations are ADMIN-only
 * and live in AdminController under /admin/room-types.
 */
@Controller
@RequestMapping("/room-types")
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    public RoomTypeController(RoomTypeService roomTypeService) {
        this.roomTypeService = roomTypeService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("roomTypes", roomTypeService.findAll());
        return "admin/room-types";
    }
}
