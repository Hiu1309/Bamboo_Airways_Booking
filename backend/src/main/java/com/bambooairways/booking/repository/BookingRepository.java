package com.bambooairways.booking.repository;

import com.bambooairways.booking.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<BookingEntity, String> {
    Optional<BookingEntity> findByBookingCode(String bookingCode);
    Optional<BookingEntity> findByBookingCodeAndUserId(String bookingCode, String userId);
    List<BookingEntity> findByUserIdOrderByCreatedAtDesc(String userId);
    List<BookingEntity> findAllByOrderByCreatedAtDesc();
}
