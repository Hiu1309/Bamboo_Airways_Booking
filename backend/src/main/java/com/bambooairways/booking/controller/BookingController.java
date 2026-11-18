package com.bambooairways.booking.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.util.MultiValueMap;

import com.bambooairways.booking.dto.BookingRequest;
import com.bambooairways.booking.dto.LoginRequest;
import com.bambooairways.booking.dto.RegisterRequest;
import com.bambooairways.booking.dto.ReviewRequest;
import com.bambooairways.booking.dto.payment.PaymentRequest;
import com.bambooairways.booking.entity.AirportEntity;
import com.bambooairways.booking.entity.BookingEntity;
import com.bambooairways.booking.entity.FlightEntity;
import com.bambooairways.booking.entity.LoyaltyEntryEntity;
import com.bambooairways.booking.entity.ReviewEntity;
import com.bambooairways.booking.entity.SeatEntity;
import com.bambooairways.booking.service.BookingService;
import com.bambooairways.enums.PaymentProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;


/**
 * Controller chỉ nhận dữ liệu từ frontend và gọi service.
 * Phần xử lý nghiệp vụ được đặt trong BookingService để dễ theo dõi.
 */
@RestController
public class BookingController {

    private final BookingService bookingService;


    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;

    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return bookingService.health();

    }

    @GetMapping("/api/airports")
    public List<AirportEntity> airports() {
        return bookingService.getAirports();

    }

    @GetMapping("/api/flights")
    public List<Map<String, Object>> flights(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "1") int passengers) {
        return bookingService.searchFlights(from, to, date, passengers);

    }

    @GetMapping("/api/flights/{id}/seats")
    public List<SeatEntity> seats(@PathVariable String id) {
        return bookingService.getSeats(id);

    }

    @PostMapping("/api/auth/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        return bookingService.register(request);

    }

    @PostMapping("/api/auth/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        return bookingService.login(request);

    }

    @PostMapping("/api/auth/refresh")
    public Map<String, Object> refresh(@RequestBody Map<String, String> request) {
        return bookingService.refresh(request);

    }

    @PostMapping("/api/auth/forgot-password")
    public Map<String, String> forgotPassword(@RequestBody Map<String, String> request) {
        return bookingService.forgotPassword(request);

    }

    @PostMapping("/api/auth/reset-password")
    public Map<String, String> resetPassword(@RequestBody Map<String, String> request) {
        return bookingService.resetPassword(request);

    }

    @PostMapping("/api/bookings")
    public ResponseEntity<?> createBooking(
            @RequestHeader("Authorization") String authorization,
            @RequestBody BookingRequest request) {
        return bookingService.createBooking(authorization, request);

    }

    @GetMapping("/api/bookings")
    public List<Map<String, Object>> myBookings(
            @RequestHeader("Authorization") String authorization) {
        return bookingService.getMyBookings(authorization);

    }

    @GetMapping("/api/bookings/{code}")
    public Map<String, Object> booking(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String code) {
        return bookingService.getBooking(authorization, code);

    }

    @PostMapping("/api/bookings/{code}/payments")
    public ResponseEntity<?> pay(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String code,
            @RequestBody PaymentRequest request) {
        return bookingService.pay(authorization, code, request);

    }

    @PostMapping("/api/payments/webhook/momo")
    public ResponseEntity<?> momoWebhook(@RequestBody String payload) throws Exception {
        JsonNode body = new ObjectMapper().readTree(payload);
        return bookingService.handlePaymentWebhook(PaymentProvider.MOMO, payload, body.path("signature").asText());
    }

    @RequestMapping(value="/api/payments/webhook/vnpay", method={RequestMethod.GET,RequestMethod.POST})
    public ResponseEntity<?> vnpayWebhook(@RequestParam MultiValueMap<String,String> params) {
        String payload = params.entrySet().stream().map(e -> e.getKey()+"="+java.net.URLEncoder.encode(e.getValue().get(0),java.nio.charset.StandardCharsets.UTF_8)).collect(java.util.stream.Collectors.joining("&"));
        return bookingService.handlePaymentWebhook(PaymentProvider.VNPAY, payload, null);
    }

    @PostMapping("/api/payments/webhook/stripe")
    public ResponseEntity<?> stripeWebhook(@RequestBody String payload, @RequestHeader(value="Stripe-Signature", required=false) String signature) {
        return bookingService.handlePaymentWebhook(PaymentProvider.STRIPE, payload, signature);
    }

    @GetMapping("/api/reviews")
    public List<ReviewEntity> reviews() {
        return bookingService.getReviews();

    }

    @PostMapping("/api/reviews")
    public ResponseEntity<?> addReview(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody ReviewRequest request) {
        return bookingService.addReview(authorization, request);

    }

    @GetMapping("/api/loyalty/transactions")
    public List<LoyaltyEntryEntity> loyalty(
            @RequestHeader("Authorization") String authorization) {
        return bookingService.getLoyalty(authorization);

    }

    @GetMapping("/api/loyalty/balance")
    public Map<String, Integer> loyaltyBalance(
            @RequestHeader("Authorization") String authorization) {
        return Map.of("points", bookingService.getLoyaltyBalance(authorization));

    }

    @GetMapping("/api/admin/bookings")
    public List<BookingEntity> adminBookings(
            @RequestHeader("Authorization") String authorization) {
        return bookingService.getAdminBookings(authorization);

    }

    @PostMapping("/api/admin/flights")
    public ResponseEntity<?> addFlight(
            @RequestHeader("Authorization") String authorization,
            @RequestBody FlightEntity flight) {
        return bookingService.addFlight(authorization, flight);

    }
}
