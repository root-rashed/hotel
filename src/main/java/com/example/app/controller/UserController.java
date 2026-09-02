package com.example.app.controller;

import com.example.app.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Account enable/disable toggle, split out from AdminController's CRUD
 * screens since it's a single-field operation triggered from the users
 * table rather than the full edit form.
 */
@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/enabled")
    public String setEnabled(@PathVariable Long id, @RequestParam boolean enabled) {
        userService.setEnabled(id, enabled);
        return "redirect:/admin/users";
    }
}
