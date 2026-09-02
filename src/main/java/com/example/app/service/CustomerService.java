package com.example.app.service;

import com.example.app.dto.CustomerDto;
import com.example.app.exception.DuplicateResourceException;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.model.entity.Customer;
import com.example.app.model.entity.User;
import com.example.app.model.enums.Role;
import com.example.app.repository.CustomerRepository;
import com.example.app.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository customerRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<CustomerDto> findAll() {
        return customerRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public CustomerDto findById(Long id) {
        return toDto(getEntity(id));
    }

    @Transactional(readOnly = true)
    public CustomerDto findByUserId(Long userId) {
        return customerRepository.findByUserId(userId)
                .map(this::toDto)
                .orElseThrow(() -> ResourceNotFoundException.of("Customer profile for user", userId));
    }

    @Transactional(readOnly = true)
    public CustomerDto findByUsername(String username) {
        return customerRepository.findByUserUsername(username)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("No customer profile for user: " + username));
    }

    public CustomerDto create(CustomerDto dto, String rawPassword) {
        if (customerRepository.existsByIdentificationNumber(dto.getIdentificationNumber())) {
            throw new DuplicateResourceException("Identification number already registered: " + dto.getIdentificationNumber());
        }

        User user;
        if (dto.getUserId() != null) {
            user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> ResourceNotFoundException.of("User", dto.getUserId()));
        } else {
            // Self-service signup path: create the linked User account with role CUSTOMER.
            if (rawPassword == null || rawPassword.isBlank()) {
                throw new IllegalArgumentException("Password is required to create a new customer account");
            }
            if (userRepository.existsByUsername(dto.getUsername())) {
                throw new DuplicateResourceException("Username already in use: " + dto.getUsername());
            }
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new DuplicateResourceException("Email already in use: " + dto.getEmail());
            }
            user = User.builder()
                    .username(dto.getUsername())
                    .email(dto.getEmail())
                    .password(passwordEncoder.encode(rawPassword))
                    .firstName(dto.getFirstName())
                    .lastName(dto.getLastName())
                    .phone(dto.getPhone())
                    .enabled(true)
                    .role(Role.CUSTOMER)
                    .build();
        }

        Customer customer = Customer.builder()
                .user(user)
                .address(dto.getAddress())
                .dateOfBirth(dto.getDateOfBirth())
                .identificationNumber(dto.getIdentificationNumber())
                .build();

        return toDto(customerRepository.save(customer));
    }

    public CustomerDto update(Long id, CustomerDto dto) {
        Customer customer = getEntity(id);
        if (!customer.getIdentificationNumber().equals(dto.getIdentificationNumber())
                && customerRepository.existsByIdentificationNumber(dto.getIdentificationNumber())) {
            throw new DuplicateResourceException("Identification number already registered: " + dto.getIdentificationNumber());
        }
        customer.setAddress(dto.getAddress());
        customer.setDateOfBirth(dto.getDateOfBirth());
        customer.setIdentificationNumber(dto.getIdentificationNumber());
        return toDto(customerRepository.save(customer));
    }

    public void delete(Long id) {
        Customer customer = getEntity(id);
        // Disable the linked user account rather than deleting the Customer row,
        // preserving booking/payment history integrity.
        customer.getUser().setEnabled(false);
        userRepository.save(customer.getUser());
    }

    private Customer getEntity(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Customer", id));
    }

    private CustomerDto toDto(Customer customer) {
        User user = customer.getUser();
        return CustomerDto.builder()
                .id(customer.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(customer.getAddress())
                .dateOfBirth(customer.getDateOfBirth())
                .identificationNumber(customer.getIdentificationNumber())
                .build();
    }
}