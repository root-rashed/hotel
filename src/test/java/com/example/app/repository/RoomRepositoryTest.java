package com.example.app.repository;

import com.example.app.model.entity.Booking;
import com.example.app.model.entity.Customer;
import com.example.app.model.entity.Room;
import com.example.app.model.entity.RoomType;
import com.example.app.model.entity.User;
import com.example.app.model.enums.BookingStatus;
import com.example.app.model.enums.Role;
import com.example.app.model.enums.RoomStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class RoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private RoomTypeRepository roomTypeRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private UserRepository userRepository;

    private Room room;

    @BeforeEach
    void setUp() {
        RoomType type = roomTypeRepository.save(RoomType.builder()
                .name("DOUBLE").capacity(2).basePrice(new BigDecimal("100.00")).build());

        room = roomRepository.save(Room.builder()
                .roomNumber("101").floor(1).status(RoomStatus.AVAILABLE)
                .pricePerNight(new BigDecimal("100.00")).roomType(type).build());

        User user = userRepository.save(User.builder()
                .username("jdoe").email("jdoe@example.com").password("hashed")
                .firstName("Jane").lastName("Doe").enabled(true).role(Role.CUSTOMER).build());

        Customer customer = customerRepository.save(Customer.builder()
                .user(user).address("1 Test St").dateOfBirth(LocalDate.of(1990, 1, 1))
                .identificationNumber("ID-TEST-1").build());

        bookingRepository.save(Booking.builder()
                .bookingReference("BKTEST001").customer(customer).room(room)
                .checkInDate(LocalDate.now().plusDays(5))
                .checkOutDate(LocalDate.now().plusDays(10))
                .numberOfGuests(2).totalAmount(new BigDecimal("500.00"))
                .bookingStatus(BookingStatus.CONFIRMED).build());
    }

    @Test
    void findAvailableRooms_excludesRoomWithOverlappingBooking() {
        List<Room> available = roomRepository.findAvailableRooms(
                LocalDate.now().plusDays(6), LocalDate.now().plusDays(8));

        assertThat(available).extracting(Room::getId).doesNotContain(room.getId());
    }

    @Test
    void findAvailableRooms_includesRoomWhenDatesDoNotOverlap() {
        List<Room> available = roomRepository.findAvailableRooms(
                LocalDate.now().plusDays(20), LocalDate.now().plusDays(22));

        assertThat(available).extracting(Room::getId).contains(room.getId());
    }

    @Test
    void findByRoomNumber_returnsSavedRoom() {
        assertThat(roomRepository.findByRoomNumber("101")).isPresent();
    }
}
