# Hotel Management System

A full-stack hotel management web app built with **Spring Boot, Spring MVC, Spring Data JPA, Spring Security, and Thymeleaf** — no frontend framework, just server-rendered HTML/CSS/vanilla JS.

## Tech Stack

Java 17 · Spring Boot 3.3 · Spring MVC · Spring Data JPA / Hibernate · MySQL · Thymeleaf (+ Spring Security extras) · Spring Security · Maven · Lombok

## Features

- Role-based access control: **ADMIN**, **RECEPTIONIST**, **CUSTOMER**
- Room & room type management
- Customer management
- Booking engine with real date-overlap availability checks (not just a status flag)
- Check-in / check-out workflow
- Payment recording
- Role-specific dashboards

## Getting Started

### 1. Prerequisites
- JDK 17+
- Maven 3.9+
- MySQL 8+ running locally (or reachable)

### 2. Configure the database

Set these environment variables (or edit `application.properties` directly for local dev):

```bash
export DB_URL="jdbc:mysql://localhost:3306/hotel_management?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true"
export DB_USERNAME=root
export DB_PASSWORD=your_password
```

The schema is created/updated automatically on startup (`spring.jpa.hibernate.ddl-auto=update`).

### 3. Run

```bash
mvn spring-boot:run
```

The app starts on **http://localhost:8080**.

### 4. Demo credentials

Seeded automatically on first run by `DataInitializer` (only if the `users` table is empty):

| Role          | Username    | Password       |
|---------------|-------------|----------------|
| ADMIN         | `admin`     | `admin123`     |
| RECEPTIONIST  | `reception` | `reception123` |
| CUSTOMER      | `customer`  | `customer123`  |

Six demo rooms across four room types (SINGLE, DOUBLE, DELUXE, SUITE) are seeded as well.

## Project Structure

```
src/main/java/com/example/app/
├── config/       SecurityConfig, CustomUserDetailsService, DataInitializer
├── controller/   MVC controllers (admin/reception/customer/shared)
├── service/      Business logic interfaces + impl/
├── repository/   Spring Data JPA repositories
├── model/entity  JPA entities
├── model/enums   Role, RoomStatus, BookingStatus, PaymentMethod, PaymentStatus
├── dto/          View/API boundary objects
└── exception/    Custom exceptions + GlobalExceptionHandler

src/main/resources/
├── templates/    Thymeleaf views (fragments/, auth/, admin/, reception/, customer/, error/)
└── static/       css/, js/
```

## Key Design Decisions

- **Availability is date-driven, not status-driven.** `Room.status` is an operational convenience flag; whether a room can actually be booked for a given date range is always computed from `Booking` records via an overlap query (`existingCheckIn < requestedCheckOut AND existingCheckOut > requestedCheckIn`), ignoring `CANCELLED` bookings. See `BookingRepository.findOverlappingBookings` and `RoomRepository.findAvailableRooms`.
- **Cascades are deliberate, not blanket.** Only `Booking → Payment` uses `CascadeType.ALL` + `orphanRemoval` (a payment has no life outside its booking). Every other relationship (RoomType→Room, Customer→Booking, Room→Booking) intentionally has **no** delete cascade, to protect historical/financial records — deletion is blocked in the service layer instead where it would orphan real data.
- **Soft deletes for accounts.** Deleting a `User` or `Customer` disables the account rather than removing the row, preserving referential integrity with any bookings/payments.
- **DTOs at every controller boundary.** Entities are never serialized directly to Thymeleaf, avoiding bidirectional-relationship loops and accidental password exposure.

## Running Tests

```bash
mvn test
```
