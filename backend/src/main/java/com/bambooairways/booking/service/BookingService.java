package com.bambooairways.booking.service;

import com.bambooairways.booking.dto.*;
import com.bambooairways.booking.entity.*;
import com.bambooairways.booking.repository.*;





import com.bambooairways.booking.dto.BookingRequest;

import com.bambooairways.booking.dto.LoginRequest;

import com.bambooairways.booking.dto.PaymentRequest;

import com.bambooairways.booking.dto.RegisterRequest;

import com.bambooairways.booking.dto.ReviewRequest;

import io.jsonwebtoken.Claims;

import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.server.ResponseStatusException;


import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;

import java.time.Duration;

import java.time.Instant;

import java.time.LocalDate;

import java.time.ZoneOffset;

import java.time.temporal.ChronoUnit;

import java.util.Arrays;

import java.util.Date;

import java.util.HexFormat;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;

import java.util.UUID;


/**
 * Các nghiệp vụ chính của website đặt vé.
 *
 * Đây là service đơn giản cho phạm vi đồ án: service gọi repository,
 * tính toán dữ liệu và trả kết quả cho controller.
 */
@Service
public class BookingService {

    @Value("${bamboo.jwt.access-secret:development-access-secret-change-in-production}")
    private String jwtSecret;


    private final UserRepository users;

    private final RefreshTokenRepository refreshTokens;

    private final PasswordResetTokenRepository resetTokens;

    private final AirportRepository airports;

    private final AircraftRepository aircraft;

    private final FlightRepository flights;

    private final FareRepository fares;

    private final SeatRepository seats;

    private final BookingRepository bookings;

    private final PassengerRepository passengers;

    private final PaymentRepository payments;

    private final ReviewRepository reviews;

    private final LoyaltyEntryRepository loyalty;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    public BookingService(
            UserRepository users,
            RefreshTokenRepository refreshTokens,
            PasswordResetTokenRepository resetTokens,
            AirportRepository airports,
            AircraftRepository aircraft,
            FlightRepository flights,
            FareRepository fares,
            SeatRepository seats,
            BookingRepository bookings,
            PassengerRepository passengers,
            PaymentRepository payments,
            ReviewRepository reviews,
            LoyaltyEntryRepository loyalty) {
        this.users = users;

        this.refreshTokens = refreshTokens;

        this.resetTokens = resetTokens;

        this.airports = airports;

        this.aircraft = aircraft;

        this.flights = flights;

        this.fares = fares;

        this.seats = seats;

        this.bookings = bookings;

        this.passengers = passengers;

        this.payments = payments;

        this.reviews = reviews;

        this.loyalty = loyalty;

    }

    public Map<String, Object> health() {
        return Map.of(
                "status", "ok",
                "service", "bamboo-airways-booking-api");

    }

    public List<AirportEntity> getAirports() {
        return airports.findAllByOrderByCodeAsc();

    }

    public List<Map<String, Object>> searchFlights(
            String from, String to, String date, int passengers) {
        Instant start = date == null
                ? Instant.EPOCH
                : LocalDate.parse(date).atStartOfDay(ZoneOffset.UTC).toInstant();

        Instant end = date == null
                ? Instant.ofEpochMilli(Long.MAX_VALUE)
                : start.plus(1, ChronoUnit.DAYS);


        AirportEntity departure = findAirport(from);

        AirportEntity arrival = findAirport(to);


        return flights
                .findByDepartureAirportIdAndArrivalAirportIdAndDepartureAtGreaterThanEqualAndDepartureAtLessThanAndStatusOrderByDepartureAtAsc(
                        departure.id, arrival.id, start, end, FlightStatus.SCHEDULED)
                .stream()
                .map(this::flightView)
                .toList();

    }

    public List<SeatEntity> getSeats(String flightId) {
        return seats.findByFlightIdOrderByCodeAsc(flightId);

    }

    public ResponseEntity<?> register(RegisterRequest request) {
        if (users.existsByUsernameOrEmail(request.username(), request.email())) {
            throw error(HttpStatus.CONFLICT,
                    "Tên đăng nhập, email hoặc giấy tờ đã tồn tại.");

        }

        UserEntity user = new UserEntity();

        user.id = UUID.randomUUID().toString();

        user.username = request.username();

        user.email = request.email();

        user.fullName = request.fullName();

        user.passwordHash = passwordEncoder.encode(request.password());

        user.memberNumber = "BAM" + (System.currentTimeMillis() % 10_000_000);

        user.identityNumber = request.identityNumber();

        user.phone = request.phone();

        user.gender = request.gender();

        user.role = UserRole.CUSTOMER;

        users.save(user);


        return ResponseEntity.status(HttpStatus.CREATED).body(createAuthResponse(user));

    }

    public Map<String, Object> login(LoginRequest request) {
        UserEntity user = users
                .findByUsernameOrEmail(request.username(), request.username())
                .orElseThrow(() -> error(
                        HttpStatus.UNAUTHORIZED,
                        "Thông tin đăng nhập không chính xác."));


        if (!passwordEncoder.matches(request.password(), user.passwordHash)) {
            throw error(HttpStatus.UNAUTHORIZED,
                    "Thông tin đăng nhập không chính xác.");

        }

        return createAuthResponse(user);

    }

    public Map<String, Object> refresh(Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        RefreshTokenEntity savedToken = refreshTokens
                .findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(
                        hash(refreshToken), Instant.now())
                .orElseThrow(() -> error(
                        HttpStatus.UNAUTHORIZED,
                        "Refresh token không hợp lệ."));


        savedToken.revokedAt = Instant.now();

        refreshTokens.save(savedToken);


        UserEntity user = users.findById(savedToken.userId).orElseThrow();

        return createAuthResponse(user);

    }

    public Map<String, String> forgotPassword(Map<String, String> request) {
        String email = request.get("email");

        users.findByUsernameOrEmail(email, email).ifPresent(user -> {
            resetTokens.deleteByUserIdAndUsedAtIsNull(user.id);


            PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity();

            resetToken.id = UUID.randomUUID().toString();

            resetToken.userId = user.id;

            resetToken.tokenHash = hash(UUID.randomUUID().toString());

            resetToken.expiresAt = Instant.now().plus(15, ChronoUnit.MINUTES);

            resetTokens.save(resetToken);

        });


        return Map.of("message",
                "Nếu email tồn tại, liên kết đặt lại mật khẩu đã được gửi.");

    }

    public Map<String, String> resetPassword(Map<String, String> request) {
        PasswordResetTokenEntity resetToken = resetTokens
                .findByTokenHashAndUsedAtIsNullAndExpiresAtAfter(
                        hash(request.get("token")), Instant.now())
                .orElseThrow(() -> error(
                        HttpStatus.BAD_REQUEST,
                        "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn."));


        UserEntity user = users.findById(resetToken.userId).orElseThrow();

        user.passwordHash = passwordEncoder.encode(request.get("password"));

        users.save(user);


        resetToken.usedAt = Instant.now();

        resetTokens.save(resetToken);

        refreshTokens.deleteByUserId(user.id);


        return Map.of("message", "Đổi mật khẩu thành công.");

    }

    @Transactional
    public ResponseEntity<?> createBooking(
            String authorization, BookingRequest request) {
        UserEntity user = getLoggedInUser(authorization);


        FareEntity fare = fares
                .findFirstByFlightIdAndCabinClassAndFareNameAndAvailableSeatsGreaterThan(
                        request.flightId(), request.cabinClass(), request.fareName(), 0)
                .orElseThrow(() -> error(
                        HttpStatus.CONFLICT, "Hạng vé không còn chỗ."));


        SeatEntity selectedSeat = null;

        int seatFee = 0;

        if (request.seatCode() != null) {
            selectedSeat = seats.findByFlightIdAndCode(
                            request.flightId(), request.seatCode())
                    .filter(seat -> "AVAILABLE".equals(seat.status))
                    .orElseThrow(() -> error(
                            HttpStatus.CONFLICT,
                            "Ghế vừa được giữ bởi hành khách khác."));

            seatFee = selectedSeat.price;

        }

        BookingEntity booking = new BookingEntity();

        booking.id = UUID.randomUUID().toString();

        booking.bookingCode = "BAM"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        booking.userId = user.id;

        booking.flightId = request.flightId();

        booking.cabinClass = request.cabinClass();

        booking.fareName = request.fareName();

        booking.contactEmail = request.contactEmail();

        booking.contactPhone = request.contactPhone();

        booking.subtotal = fare.price;

        booking.tax = Math.round(fare.price * 0.306f);

        booking.seatFee = seatFee;

        booking.total = booking.subtotal + booking.tax + booking.seatFee;

        booking.expiresAt = Instant.now().plus(20, ChronoUnit.MINUTES);

        booking.createdAt = Instant.now();

        booking.status = BookingStatus.PENDING;

        bookings.save(booking);


        fare.availableSeats--;

        fares.save(fare);


        if (selectedSeat != null) {
            selectedSeat.status = "HELD";

            selectedSeat.bookingId = booking.id;

            seats.save(selectedSeat);

        }

        savePassenger(booking, request.passenger());

        return ResponseEntity.status(HttpStatus.CREATED).body(bookingView(booking));

    }

    public List<Map<String, Object>> getMyBookings(String authorization) {
        UserEntity user = getLoggedInUser(authorization);

        return bookings.findByUserIdOrderByCreatedAtDesc(user.id)
                .stream()
                .map(this::bookingView)
                .toList();

    }

    public Map<String, Object> getBooking(String authorization, String code) {
        UserEntity user = getLoggedInUser(authorization);

        BookingEntity booking = bookings.findByBookingCodeAndUserId(code, user.id)
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND, "Không tìm thấy mã đặt chỗ."));

        return bookingView(booking);

    }

    @Transactional
    public ResponseEntity<?> pay(
            String authorization, String code, PaymentRequest request) {
        UserEntity user = getLoggedInUser(authorization);

        BookingEntity booking = bookings.findByBookingCodeAndUserId(code, user.id)
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND, "Đặt chỗ không hợp lệ."));


        PaymentEntity payment = new PaymentEntity();

        payment.id = UUID.randomUUID().toString();

        payment.bookingId = booking.id;

        payment.method = request.method();

        payment.amount = booking.total;

        payment.status = PaymentStatus.PAID;

        payment.providerRef = "DEMO-" + System.currentTimeMillis();

        payment.paidAt = Instant.now();

        payment.createdAt = Instant.now();

        payments.save(payment);


        booking.status = BookingStatus.CONFIRMED;

        bookings.save(booking);


        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "payment", payment,
                "bookingCode", booking.bookingCode));

    }

    public List<ReviewEntity> getReviews() {
        return reviews.findByApprovedTrueOrderByCreatedAtDesc();

    }

    public ResponseEntity<?> addReview(
            String authorization, ReviewRequest request) {
        UserEntity user = getLoggedInUser(authorization);


        ReviewEntity review = new ReviewEntity();

        review.id = UUID.randomUUID().toString();

        review.userId = user.id;

        review.rating = request.rating();

        review.comment = request.comment();

        review.approved = true;

        review.createdAt = Instant.now();


        return ResponseEntity.status(HttpStatus.CREATED).body(reviews.save(review));

    }

    public List<LoyaltyEntryEntity> getLoyalty(String authorization) {
        UserEntity user = getLoggedInUser(authorization);

        return loyalty.findByUserIdOrderByCreatedAtDesc(user.id);

    }

    public List<BookingEntity> getAdminBookings(String authorization) {
        checkAdmin(authorization);

        return bookings.findAllByOrderByCreatedAtDesc();

    }

    public ResponseEntity<?> addFlight(
            String authorization, FlightEntity flight) {
        checkAdmin(authorization);

        if (flight.id == null) {
            flight.id = UUID.randomUUID().toString();

        }
        return ResponseEntity.status(HttpStatus.CREATED).body(flights.save(flight));

    }

    private void savePassenger(
            BookingEntity booking, BookingRequest.PassengerRequest request) {
        if (request == null) {
            return;

        }

        PassengerEntity passenger = new PassengerEntity();

        passenger.id = UUID.randomUUID().toString();

        passenger.bookingId = booking.id;

        passenger.title = request.title();

        passenger.firstName = request.firstName();

        passenger.lastName = request.lastName();

        passenger.identityNumber = request.identityNumber();

        passenger.memberNumber = request.memberNumber();

        passengers.save(passenger);

    }

    private AirportEntity findAirport(String code) {
        return airports.findByCode(code.toUpperCase())
                .orElseThrow(() -> error(
                        HttpStatus.BAD_REQUEST, "Sân bay không tồn tại."));

    }

    private UserEntity getLoggedInUser(String authorization) {
        if (authorization == null
                || !authorization.matches("(?i)Bearer\\s+.+")) {
            throw error(HttpStatus.UNAUTHORIZED, "Thiếu access token.");

        }

        try {
            String token = authorization.substring(authorization.indexOf(' ') + 1);

            Claims claims = Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();


            return users.findById(claims.get("id", String.class))
                    .orElseThrow();

        } catch (Exception exception) {
            throw error(HttpStatus.UNAUTHORIZED,
                    "Access token không hợp lệ hoặc đã hết hạn.");

        }
    }

    private UserEntity checkAdmin(String authorization) {
        UserEntity user = getLoggedInUser(authorization);

        if (user.role != UserRole.ADMIN) {
            throw error(HttpStatus.FORBIDDEN,
                    "Chỉ quản trị viên được phép thực hiện.");

        }
        return user;

    }

    private Map<String, Object> createAuthResponse(UserEntity user) {
        String accessToken = Jwts.builder()
                .subject(user.email)
                .claim("id", user.id)
                .claim("role", user.role.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 900_000))
                .signWith(signingKey())
                .compact();


        String refreshToken = UUID.randomUUID().toString();

        RefreshTokenEntity savedToken = new RefreshTokenEntity();

        savedToken.id = UUID.randomUUID().toString();

        savedToken.tokenHash = hash(refreshToken);

        savedToken.userId = user.id;

        savedToken.expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);

        savedToken.createdAt = Instant.now();

        refreshTokens.save(savedToken);


        return Map.of(
                "user", Map.of(
                        "id", user.id,
                        "username", user.username,
                        "email", user.email,
                        "fullName", user.fullName,
                        "role", user.role.name(),
                        "memberNumber", user.memberNumber),
                "accessToken", accessToken,
                "refreshToken", refreshToken);

    }

    private SecretKey signingKey() {
        byte[] secretBytes = Arrays.copyOf(
                jwtSecret.getBytes(StandardCharsets.UTF_8), 32);

        return Keys.hmacShaKeyFor(secretBytes);

    }

    private String hash(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(bytes);

        } catch (Exception exception) {
            throw new IllegalStateException("Không thể tạo mã băm.", exception);

        }
    }

    private Map<String, Object> flightView(FlightEntity flight) {
        Map<String, Object> result = new LinkedHashMap<>();
        AirportEntity departure = airports.findById(flight.departureAirportId).orElseThrow();
        AirportEntity arrival = airports.findById(flight.arrivalAirportId).orElseThrow();
        AircraftEntity aircraft = new AircraftEntity();
        if (flight.aircraftId != null) {
            aircraft = this.aircraft.findById(flight.aircraftId).orElse(aircraft);
        }

        result.put("id", flight.id);

        result.put("flightNumber", flight.flightNumber);

        result.put("departureAirportId", flight.departureAirportId);

        result.put("arrivalAirportId", flight.arrivalAirportId);
        result.put("departureAirport", Map.of(
                "code", departure.code,
                "name", departure.name,
                "city", departure.city));
        result.put("arrivalAirport", Map.of(
                "code", arrival.code,
                "name", arrival.name,
                "city", arrival.city));
        result.put("aircraft", Map.of("model", aircraft.model == null ? "" : aircraft.model));

        result.put("departureAt", flight.departureAt);

        result.put("arrivalAt", flight.arrivalAt);

        result.put("basePrice", flight.basePrice);

        result.put("businessPrice", flight.businessPrice);

        result.put("status", flight.status);
        result.put("fares", fares.findByFlightIdOrderByPriceAsc(flight.id).stream()
                .map(fare -> Map.of(
                        "cabinClass", fare.cabinClass,
                        "fareName", fare.fareName,
                        "price", fare.price,
                        "availableSeats", fare.availableSeats))
                .toList());

        result.put("durationMinutes",
                Duration.between(flight.departureAt, flight.arrivalAt).toMinutes());

        return result;

    }

    private Map<String, Object> bookingView(BookingEntity booking) {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("id", booking.id);

        result.put("bookingCode", booking.bookingCode);

        result.put("userId", booking.userId);

        result.put("flightId", booking.flightId);

        result.put("status", booking.status);

        result.put("cabinClass", booking.cabinClass);

        result.put("fareName", booking.fareName);

        result.put("contactEmail", booking.contactEmail);

        result.put("contactPhone", booking.contactPhone);

        result.put("subtotal", booking.subtotal);

        result.put("tax", booking.tax);

        result.put("seatFee", booking.seatFee);

        result.put("total", booking.total);

        result.put("passengers", passengers.findByBookingId(booking.id));

        result.put("seats", seats.findByFlightIdOrderByCodeAsc(booking.flightId)
                .stream()
                .filter(seat -> booking.id.equals(seat.bookingId))
                .toList());

        result.put("payments", payments.findByBookingId(booking.id));

        return result;

    }

    private ResponseStatusException error(HttpStatus status, String message) {
        return new ResponseStatusException(status, message);

    }
}
