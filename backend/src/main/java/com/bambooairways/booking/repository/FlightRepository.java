package com.bambooairways.booking.repository;

import com.bambooairways.booking.entity.FlightEntity;
import com.bambooairways.booking.entity.FlightStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface FlightRepository extends JpaRepository<FlightEntity, String> {
    Optional<FlightEntity> findByFlightNumber(String flightNumber);
    List<FlightEntity> findByDepartureAirportIdAndArrivalAirportIdAndDepartureAtGreaterThanEqualAndDepartureAtLessThanAndStatusOrderByDepartureAtAsc(
            String departureAirportId, String arrivalAirportId, Instant start, Instant end, FlightStatus status);
}
