package com.example.app.service;

import com.example.app.dto.RoomTypeDto;
import com.example.app.exception.BookingException;
import com.example.app.exception.DuplicateResourceException;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.model.entity.RoomType;
import com.example.app.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;

    public RoomTypeService(RoomTypeRepository roomTypeRepository) {
        this.roomTypeRepository = roomTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<RoomTypeDto> findAll() {
        return roomTypeRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public RoomTypeDto findById(Long id) {
        return toDto(getEntity(id));
    }

    public RoomTypeDto create(RoomTypeDto dto) {
        if (roomTypeRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException("Room type already exists: " + dto.getName());
        }
        RoomType roomType = RoomType.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .capacity(dto.getCapacity())
                .basePrice(dto.getBasePrice())
                .build();
        return toDto(roomTypeRepository.save(roomType));
    }

    public RoomTypeDto update(Long id, RoomTypeDto dto) {
        RoomType roomType = getEntity(id);
        if (!roomType.getName().equals(dto.getName()) && roomTypeRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException("Room type already exists: " + dto.getName());
        }
        roomType.setName(dto.getName());
        roomType.setDescription(dto.getDescription());
        roomType.setCapacity(dto.getCapacity());
        roomType.setBasePrice(dto.getBasePrice());
        return toDto(roomTypeRepository.save(roomType));
    }

    public void delete(Long id) {
        RoomType roomType = getEntity(id);
        // Guard against orphaning real rooms — deletion is blocked at the
        // service layer rather than relying on a destructive cascade.
        if (!roomType.getRooms().isEmpty()) {
            throw new BookingException("Cannot delete room type '" + roomType.getName()
                    + "' while rooms are still assigned to it.");
        }
        roomTypeRepository.delete(roomType);
    }

    private RoomType getEntity(Long id) {
        return roomTypeRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("RoomType", id));
    }

    private RoomTypeDto toDto(RoomType roomType) {
        return RoomTypeDto.builder()
                .id(roomType.getId())
                .name(roomType.getName())
                .description(roomType.getDescription())
                .capacity(roomType.getCapacity())
                .basePrice(roomType.getBasePrice())
                .roomCount(roomType.getRooms().size())
                .build();
    }
}