package com.example.app.service;

import com.example.app.dto.BookingDto;
import com.example.app.dto.BookingRequestDto;
import com.example.app.exception.BookingException;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.exception.UnauthorizedException;
import com.example.app.model.entity.Booking;
import com.example.app.model.entity.Customer;
import com.example.app.model.entity.Room;
import com.example.app.model.entity.User;
import com.example.app.model.enums.BookingStatus;
import com.example.app.model.enums.RoomStatus;
import com.example.app.repository.BookingRepository;
import com.example.app.repository.CustomerRepository;
import com.example.app.repository.RoomRepository;
import com.example.app.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class BookingService {

    private static final String REF_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
                          RoomRepository roomRepository,
                          CustomerRepository customerRepository,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<BookingDto> findAll() {
        return bookingRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public BookingDto findById(Long id) {
        return toDto(getEntity(id));
    }

    @Transactional(readOnly = true)
    public List<BookingDto> findByCustomerUsername(String username) {
        Customer customer = customerRepository.findByUserId(resolveUserId(username))
                .orElseThrow(() -> new ResourceNotFoundException("No customer profile for user: " + username));
        return bookingRepository.findByCustomerId(customer.getId()).stream().map(this::toDto).toList();
    }

    /**
     * Creates a booking as staff (ADMIN/RECEPTIONIST) on behalf of any customer.
     */
    public BookingDto createByStaff(BookingRequestDto request) {
        if (request.getCustomerId() == null) {
            throw new BookingException("customerId is required when staff create a booking.");
        }
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> ResourceNotFoundException.of("Customer", request.getCustomerId()));
        return toDto(createBooking(customer, request));
    }

    /**
     * Creates a booking as the authenticated customer themselves; customerId
     * on the request is ignored and resolved from the principal.
     */
    public BookingDto createBySelf(BookingRequestDto request, String username) {
        Customer customer = customerRepository.findByUserId(resolveUserId(username))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No customer profile found for the current user. Please complete your profile first."));
        return toDto(createBooking(customer, request));
    }

    private Booking createBooking(Customer customer, BookingRequestDto request) {
        validateDates(request.getCheckInDate(), request.getCheckOutDate());

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> ResourceNotFoundException.of("Room", request.getRoomId()));

        if (room.getStatus() == RoomStatus.MAINTENANCE) {
            throw new BookingException("Room " + room.getRoomNumber() + " is under maintenance and cannot be booked.");
        }

        // Ignore Room.status for date-based availability — check for real
        // overlapping, non-cancelled bookings instead.
        List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                room.getId(), request.getCheckInDate(), request.getCheckOutDate());
        if (!overlapping.isEmpty()) {
            throw new BookingException("Room " + room.getRoomNumber()
                    + " is not available for the selected dates.");
        }

        if (request.getNumberOfGuests() > room.getRoomType().getCapacity()) {
            throw new BookingException("Room " + room.getRoomNumber() + " has a maximum capacity of "
                    + room.getRoomType().getCapacity() + " guests.");
        }

        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        BigDecimal totalAmount = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        Booking booking = Booking.builder()
                .bookingReference(generateBookingReference())
                .customer(customer)
                .room(room)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .numberOfGuests(request.getNumberOfGuests())
                .totalAmount(totalAmount)
                .bookingStatus(BookingStatus.PENDING)
                .build();

        Booking saved = bookingRepository.save(booking);

        // Reflect the reservation on the room's operational status. This is a
        // display/ops convenience, not the source of truth for date-based
        // availability (that's always computed from Booking records).
        room.setStatus(RoomStatus.RESERVED);
        roomRepository.save(room);

        return saved;
    }

    public BookingDto updateStatus(Long id, BookingStatus status) {
        Booking booking = getEntity(id);
        booking.setBookingStatus(status);
        return toDto(bookingRepository.save(booking));
    }

    /**
     * Cancels a booking. If requestedByUsername is non-null, enforces that
     * the booking belongs to that user's customer profile (self-service
     * cancellation); pass null for staff-initiated cancellation.
     */
    public void cancel(Long id, String requestedByUsername) {
        Booking booking = getEntity(id);

        if (requestedByUsername != null) {
            Customer ownerCustomer = customerRepository.findByUserId(resolveUserId(requestedByUsername))
                    .orElseThrow(() -> new UnauthorizedException("No customer profile for current user."));
            if (!booking.getCustomer().getId().equals(ownerCustomer.getId())) {
                throw new UnauthorizedException("You are not allowed to cancel another customer's booking.");
            }
        }

        if (booking.getBookingStatus() == BookingStatus.CHECKED_OUT) {
            throw new BookingException("A completed (checked-out) booking cannot be cancelled.");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Freeing the room is safe here: with the booking now CANCELLED it is
        // excluded from overlap checks, so date-based availability is correct
        // regardless of this operational status flag.
        Room room = booking.getRoom();
        room.setStatus(RoomStatus.AVAILABLE);
        roomRepository.save(room);
    }

    public BookingDto checkIn(Long id) {
        Booking booking = getEntity(id);
        if (booking.getBookingStatus() != BookingStatus.CONFIRMED
                && booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new BookingException("Only pending or confirmed bookings can be checked in.");
        }
        booking.setBookingStatus(BookingStatus.CHECKED_IN);
        bookingRepository.save(booking);

        Room room = booking.getRoom();
        room.setStatus(RoomStatus.OCCUPIED);
        roomRepository.save(room);

        return toDto(booking);
    }

    public BookingDto checkOut(Long id) {
        Booking booking = getEntity(id);
        if (booking.getBookingStatus() != BookingStatus.CHECKED_IN) {
            throw new BookingException("Only checked-in bookings can be checked out.");
        }
        booking.setBookingStatus(BookingStatus.CHECKED_OUT);
        bookingRepository.save(booking);

        Room room = booking.getRoom();
        room.setStatus(RoomStatus.AVAILABLE);
        roomRepository.save(room);

        return toDto(booking);
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new BookingException("Check-in and check-out dates are required.");
        }
        if (!checkIn.isBefore(checkOut)) {
            throw new BookingException("Check-in date must be before check-out date.");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new BookingException("Check-in date cannot be in the past.");
        }
    }


    private String generateBookingReference() {
        String candidate;
        do {
            StringBuilder sb = new StringBuilder("BK");
            for (int i = 0; i < 8; i++) {
                sb.append(REF_ALPHABET.charAt(RANDOM.nextInt(REF_ALPHABET.length())));
            }
            candidate = sb.toString();
        } while (bookingRepository.findByBookingReference(candidate).isPresent());
        return candidate;
    }

    private Long resolveUserId(String username) {
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private Booking getEntity(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Booking", id));
    }

    private BookingDto toDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .customerId(booking.getCustomer().getId())
                .customerName(booking.getCustomer().getUser().getFirstName() + " "
                        + booking.getCustomer().getUser().getLastName())
                .roomId(booking.getRoom().getId())
                .roomNumber(booking.getRoom().getRoomNumber())
                .roomTypeName(booking.getRoom().getRoomType().getName())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .numberOfGuests(booking.getNumberOfGuests())
                .totalAmount(booking.getTotalAmount())
                .bookingStatus(booking.getBookingStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}