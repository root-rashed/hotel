package com.example.app.controller;

import com.example.app.dto.PaymentDto;
import com.example.app.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Payment recording is ADMIN/RECEPTIONIST-only; both AdminController and
 * ReceptionController link into this shared endpoint rather than duplicating
 * the persistence call.
 */
@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @PostMapping("/record")
    public String record(@Valid @ModelAttribute("paymentDto") PaymentDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/reception/payments";
        }
        paymentService.record(dto);
        return "redirect:/reception/payments";
    }
}
