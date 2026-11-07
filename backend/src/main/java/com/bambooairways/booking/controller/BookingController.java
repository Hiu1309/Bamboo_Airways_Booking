package com.bambooairways.booking.controller;

import com.bambooairways.booking.dto.*;
import com.bambooairways.booking.entity.*;
import com.bambooairways.booking.service.BookingService;





import com.bambooairways.booking.dto.BookingRequest;

import com.bambooairways.booking.dto.LoginRequest;

import com.bambooairways.booking.dto.PaymentRequest;

import com.bambooairways.booking.dto.RegisterRequest;

import com.bambooairways.booking.dto.ReviewRequest;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;


import java.util.List;

import java.util.Map;


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
