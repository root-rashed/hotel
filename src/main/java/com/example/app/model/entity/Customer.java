package com.example.app.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owning side of the User <-> Customer relationship (one profile per user).
    // Cascade limited to PERSIST/MERGE: saving a Customer can create/update the
    // linked User in the same operation, but deleting a Customer row must never
    // cascade-delete the underlying User account.
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String address;

    @Past
    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @NotBlank
    @Column(nullable = false, unique = true, length = 50)
    private String identificationNumber;

    // No cascade REMOVE: bookings must survive even if a customer profile is
    // deactivated. Historical/financial integrity takes priority.
    @Builder.Default
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();
}
