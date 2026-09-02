package com.example.app.controller;

import com.example.app.dto.CustomerDto;
import com.example.app.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final CustomerService customerService;

    public AuthController(CustomerService customerService) {
        this.customerService = customerService;
    }



    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                             @RequestParam(required = false) String logout,
                             Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password.");
        }
        if (logout != null) {
            model.addAttribute("infoMessage", "You have been logged out.");
        }
        return "auth/login";
    }



    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("customerDto", CustomerDto.builder().build());
        return "auth/register";
    }



    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("customerDto") CustomerDto customerDto,
                            BindingResult bindingResult,
                            @RequestParam String password,
                            Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        customerService.create(customerDto, password);
        model.addAttribute("infoMessage", "Account created successfully. Please log in.");
        return "auth/login";
    }
}
