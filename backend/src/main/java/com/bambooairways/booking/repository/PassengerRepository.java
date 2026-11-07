package com.bambooairways.booking.repository;

import com.bambooairways.booking.entity.PassengerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PassengerRepository extends JpaRepository<PassengerEntity, String> {
    List<PassengerEntity> findByBookingId(String bookingId);
}
