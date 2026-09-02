package com.example.app.dto;

import com.example.app.model.enums.RoomStatus;
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
public class RoomDto {

    private Long id;

    @NotBlank
    private String roomNumber;

    @NotNull
    @Positive
    private Integer floor;

    @NotNull
    private RoomStatus status;

    @NotNull
    @Positive
    private BigDecimal pricePerNight;

    private String description;

    @NotNull
    private Long roomTypeId;

    private String roomTypeName;
}
