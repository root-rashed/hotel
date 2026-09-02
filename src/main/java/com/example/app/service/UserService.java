package com.example.app.service;

import com.example.app.dto.UserDto;
import com.example.app.exception.DuplicateResourceException;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.model.entity.User;
import com.example.app.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
        return toDto(getEntity(id));
    }

    public UserDto create(UserDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Username already in use: " + dto.getUsername());
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + dto.getEmail());
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required when creating a user");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phone(dto.getPhone())
                .enabled(true)
                .role(dto.getRole())
                .build();

        return toDto(userRepository.save(user));
    }

    public UserDto update(Long id, UserDto dto) {
        User user = getEntity(id);

        if (!user.getUsername().equals(dto.getUsername()) && userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Username already in use: " + dto.getUsername());
        }
        if (!user.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + dto.getEmail());
        }

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole());

        // Only re-encode/update the password if a new one was actually submitted.
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return toDto(userRepository.save(user));
    }

    public void delete(Long id) {
        User user = getEntity(id);
        // Soft-delete pattern: disable rather than hard-delete, to preserve
        // referential integrity with historical bookings/payments tied
        // indirectly through a Customer profile.
        user.setEnabled(false);
        userRepository.save(user);
    }

    public void setEnabled(Long id, boolean enabled) {
        User user = getEntity(id);
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    private User getEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }




    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .enabled(user.isEnabled())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}