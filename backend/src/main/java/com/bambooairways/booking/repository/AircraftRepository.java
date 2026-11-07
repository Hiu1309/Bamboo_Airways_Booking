package com.bambooairways.booking.repository;

import com.bambooairways.booking.entity.AircraftEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AircraftRepository extends JpaRepository<AircraftEntity, String> {
    Optional<AircraftEntity> findByCode(String code);
}
