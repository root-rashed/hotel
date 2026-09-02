package com.example.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomTypeDto {

    private Long id;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    @Positive
    private Integer capacity;

    @NotNull
    @Positive
    private BigDecimal basePrice;

    private int roomCount;
}
