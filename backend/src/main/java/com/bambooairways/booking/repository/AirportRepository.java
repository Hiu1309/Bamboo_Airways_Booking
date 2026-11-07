package com.bambooairways.booking.repository;

import com.bambooairways.booking.entity.AirportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AirportRepository extends JpaRepository<AirportEntity, String> {
    Optional<AirportEntity> findByCode(String code);
    List<AirportEntity> findAllByOrderByCodeAsc();
}
