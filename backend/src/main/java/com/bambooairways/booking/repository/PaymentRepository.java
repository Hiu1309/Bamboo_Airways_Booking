package com.bambooairways.booking.repository;

import com.bambooairways.booking.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRepository extends JpaRepository<PaymentEntity, String> {
    List<PaymentEntity> findByBookingId(String bookingId);
}
