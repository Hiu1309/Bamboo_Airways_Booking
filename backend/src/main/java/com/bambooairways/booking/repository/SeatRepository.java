package com.bambooairways.booking.repository;

import com.bambooairways.booking.entity.SeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<SeatEntity, String> {
    List<SeatEntity> findByFlightIdOrderByCodeAsc(String flightId);
    Optional<SeatEntity> findByFlightIdAndCode(String flightId, String code);
}
