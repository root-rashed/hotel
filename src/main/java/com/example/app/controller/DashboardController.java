package com.example.app.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    /**
     * Generic post-login landing point. In practice SecurityConfig's
     * role-based success handler sends users straight to their own dashboard,
     * so this mainly exists as a safe fallback / deep-link target.
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        String topRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");

        return switch (topRole) {
            case "ROLE_ADMIN" -> "redirect:/admin/dashboard";
            case "ROLE_RECEPTIONIST" -> "redirect:/reception/dashboard";
            case "ROLE_CUSTOMER" -> "redirect:/customer/dashboard";
            default -> "redirect:/login";
        };
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }
}
