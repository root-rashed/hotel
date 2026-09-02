package com.example.app.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(UnauthorizedException.class)
    public String handleUnauthorized(UnauthorizedException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/403";
    }

    @ExceptionHandler({BookingException.class, DuplicateResourceException.class})
    public String handleBusinessRuleViolation(RuntimeException ex, Model model, HttpServletRequest request) {
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("requestUri", request.getRequestURI());
        return "error/error";
    }

    @ExceptionHandler(BindException.class)
    public String handleValidation(BindException ex, Model model) {
        model.addAttribute("errorMessage", "Please correct the highlighted fields and try again.");
        return "error/error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception ex, Model model) {
        model.addAttribute("errorMessage", "An unexpected error occurred. Please try again later.");
        return "error/error";
    }
}
