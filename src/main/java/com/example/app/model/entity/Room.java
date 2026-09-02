package com.example.app.model.entity;

import com.example.app.model.enums.RoomStatus;
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
@Table(name = "rooms", uniqueConstraints = @UniqueConstraint(columnNames = "roomNumber"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 10)
    private String roomNumber;

    @Positive
    @Column(nullable = false)
    private Integer floor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status;

    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    @Column(length = 500)
    private String description;

    // Owning side of the RoomType <-> Room relationship.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    // No cascade REMOVE: bookings are historical/financial records and must
    // outlive a room being taken offline or deleted.
    @Builder.Default
    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();
}
