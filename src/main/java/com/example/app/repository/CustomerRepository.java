package com.example.app.repository;

import com.example.app.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUserId(Long userId);

    Optional<Customer> findByUserUsername(String username);

    Optional<Customer> findByIdentificationNumber(String identificationNumber);

    boolean existsByIdentificationNumber(String identificationNumber);
}
