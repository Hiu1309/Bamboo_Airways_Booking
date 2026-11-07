package com.bambooairways.booking.repository;

import com.bambooairways.booking.entity.FareEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FareRepository extends JpaRepository<FareEntity, String> {
    List<FareEntity> findByFlightIdOrderByPriceAsc(String flightId);

    Optional<FareEntity> findFirstByFlightIdAndCabinClassAndFareNameAndAvailableSeatsGreaterThan(
            String flightId, String cabinClass, String fareName, int seats);
}
