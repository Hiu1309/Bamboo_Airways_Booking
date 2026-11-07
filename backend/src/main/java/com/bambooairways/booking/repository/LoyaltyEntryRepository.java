package com.bambooairways.booking.repository;

import com.bambooairways.booking.entity.LoyaltyEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoyaltyEntryRepository extends JpaRepository<LoyaltyEntryEntity, String> {
    List<LoyaltyEntryEntity> findByUserIdOrderByCreatedAtDesc(String userId);
}
