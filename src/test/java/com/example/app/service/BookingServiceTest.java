package com.example.app.service;

import com.example.app.dto.BookingRequestDto;
import com.example.app.exception.BookingException;
import com.example.app.model.entity.Customer;
import com.example.app.model.entity.Room;
import com.example.app.model.entity.RoomType;
import com.example.app.model.entity.User;
import com.example.app.model.enums.RoomStatus;
import com.example.app.repository.BookingRepository;
import com.example.app.repository.CustomerRepository;
import com.example.app.repository.RoomRepository;
import com.example.app.repository.UserRepository;
import com.example.app.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingService bookingService;

    private Room room;
    private Customer customer;

    @BeforeEach
    void setUp() {
        RoomType roomType = RoomType.builder().id(1L).name("DOUBLE").capacity(2)
                .basePrice(new BigDecimal("100.00")).build();
        room = Room.builder().id(1L).roomNumber("101").floor(1).status(RoomStatus.AVAILABLE)
                .pricePerNight(new BigDecimal("100.00")).roomType(roomType).build();

        User user = User.builder().id(1L).username("jdoe").build();
        customer = Customer.builder().id(1L).user(user).address("1 Test St")
                .dateOfBirth(LocalDate.of(1990, 1, 1)).identificationNumber("ID-1").build();
    }

    @Test
    void createByStaff_rejectsCheckOutBeforeCheckIn() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        BookingRequestDto request = BookingRequestDto.builder()
                .customerId(1L).roomId(1L)
                .checkInDate(LocalDate.now().plusDays(10))
                .checkOutDate(LocalDate.now().plusDays(5))
                .numberOfGuests(2)
                .build();

        assertThatThrownBy(() -> bookingService.createByStaff(request))
                .isInstanceOf(BookingException.class)
                .hasMessageContaining("before");
    }

    @Test
    void createByStaff_rejectsOverlappingBooking() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any()))
                .thenReturn(List.of(com.example.app.model.entity.Booking.builder().id(99L).build()));

        BookingRequestDto request = BookingRequestDto.builder()
                .customerId(1L).roomId(1L)
                .checkInDate(LocalDate.now().plusDays(5))
                .checkOutDate(LocalDate.now().plusDays(10))
                .numberOfGuests(2)
                .build();

        assertThatThrownBy(() -> bookingService.createByStaff(request))
                .isInstanceOf(BookingException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void createByStaff_rejectsGuestCountOverCapacity() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        BookingRequestDto request = BookingRequestDto.builder()
                .customerId(1L).roomId(1L)
                .checkInDate(LocalDate.now().plusDays(5))
                .checkOutDate(LocalDate.now().plusDays(10))
                .numberOfGuests(5) // room capacity is 2
                .build();

        assertThatThrownBy(() -> bookingService.createByStaff(request))
                .isInstanceOf(BookingException.class)
                .hasMessageContaining("capacity");
    }

    @Test
    void createByStaff_computesTotalAmountFromNightsAndPrice() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.findByBookingReference(any())).thenReturn(Optional.empty());
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        BookingRequestDto request = BookingRequestDto.builder()
                .customerId(1L).roomId(1L)
                .checkInDate(LocalDate.now().plusDays(5))
                .checkOutDate(LocalDate.now().plusDays(8)) // 3 nights
                .numberOfGuests(2)
                .build();

        var result = bookingService.createByStaff(request);

        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("300.00"));
    }
}
