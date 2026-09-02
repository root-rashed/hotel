package com.example.app.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // e.g. SINGLE, DOUBLE, DELUXE, SUITE
    @NotBlank
    @Column(nullable = false, unique = true, length = 40)
    private String name;

    @Column(length = 500)
    private String description;

    @Positive
    @Column(nullable = false)
    private Integer capacity;

    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    // No cascade REMOVE: deleting a RoomType must never silently delete real
    // rooms tied to it. The service layer blocks deletion while rooms exist.
    @Builder.Default
    @OneToMany(mappedBy = "roomType", fetch = FetchType.LAZY)
    private List<Room> rooms = new ArrayList<>();
}
