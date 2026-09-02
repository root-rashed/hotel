package com.example.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDto {

    private Long id;

    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    @NotBlank
    private String address;

    @NotNull
    @Past
    private LocalDate dateOfBirth;

    @NotBlank
    private String identificationNumber;
}
