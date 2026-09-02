package com.example.app.service;

import com.example.app.dto.RoomDto;
import com.example.app.exception.BookingException;
import com.example.app.exception.DuplicateResourceException;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.model.entity.Room;
import com.example.app.model.entity.RoomType;
import com.example.app.model.enums.RoomStatus;
import com.example.app.repository.RoomRepository;
import com.example.app.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;

    public RoomService(RoomRepository roomRepository, RoomTypeRepository roomTypeRepository) {
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<RoomDto> findAll() {
        return roomRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public RoomDto findById(Long id) {
        return toDto(getEntity(id));
    }

    public RoomDto create(RoomDto dto) {
        if (roomRepository.existsByRoomNumber(dto.getRoomNumber())) {
            throw new DuplicateResourceException("Room number already exists: " + dto.getRoomNumber());
        }
        RoomType roomType = roomTypeRepository.findById(dto.getRoomTypeId())
                .orElseThrow(() -> ResourceNotFoundException.of("RoomType", dto.getRoomTypeId()));

        Room room = Room.builder()
                .roomNumber(dto.getRoomNumber())
                .floor(dto.getFloor())
                .status(dto.getStatus() != null ? dto.getStatus() : RoomStatus.AVAILABLE)
                .pricePerNight(dto.getPricePerNight())
                .description(dto.getDescription())
                .roomType(roomType)
                .build();

        return toDto(roomRepository.save(room));
    }

    public RoomDto update(Long id, RoomDto dto) {
        Room room = getEntity(id);
        if (!room.getRoomNumber().equals(dto.getRoomNumber()) && roomRepository.existsByRoomNumber(dto.getRoomNumber())) {
            throw new DuplicateResourceException("Room number already exists: " + dto.getRoomNumber());
        }
        RoomType roomType = roomTypeRepository.findById(dto.getRoomTypeId())
                .orElseThrow(() -> ResourceNotFoundException.of("RoomType", dto.getRoomTypeId()));

        room.setRoomNumber(dto.getRoomNumber());
        room.setFloor(dto.getFloor());
        room.setStatus(dto.getStatus());
        room.setPricePerNight(dto.getPricePerNight());
        room.setDescription(dto.getDescription());
        room.setRoomType(roomType);

        return toDto(roomRepository.save(room));
    }

    public void delete(Long id) {
        Room room = getEntity(id);
        if (!room.getBookings().isEmpty()) {
            throw new BookingException("Cannot delete room '" + room.getRoomNumber()
                    + "' — it has booking history. Set it to MAINTENANCE instead.");
        }
        roomRepository.delete(room);
    }

    public void updateStatus(Long id, RoomStatus status) {
        Room room = getEntity(id);
        room.setStatus(status);
        roomRepository.save(room);
    }

    @Transactional(readOnly = true)
    public List<RoomDto> findAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkIn.isBefore(checkOut)) {
            throw new BookingException("Check-in date must be before check-out date.");
        }
        return roomRepository.findAvailableRooms(checkIn, checkOut).stream().map(this::toDto).toList();
    }

    private Room getEntity(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Room", id));
    }

    private RoomDto toDto(Room room) {
        return RoomDto.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .floor(room.getFloor())
                .status(room.getStatus())
                .pricePerNight(room.getPricePerNight())
                .description(room.getDescription())
                .roomTypeId(room.getRoomType().getId())
                .roomTypeName(room.getRoomType().getName())
                .build();
    }
}