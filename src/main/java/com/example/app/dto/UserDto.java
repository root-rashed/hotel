package com.example.app.dto;

import com.example.app.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Used for both display and create/update. `password` is only populated
 * (and required) when creating a new user or explicitly resetting a password;
 * it is NEVER populated when reading a user back out for display.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Email
    private String email;

    // Write-only: bound from the form on create/reset, never rendered back.
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String phone;

    private boolean enabled;

    @NotNull
    private Role role;

    private LocalDateTime createdAt;
}
