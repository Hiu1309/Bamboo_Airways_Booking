package com.bambooairways.booking.repository;

import com.bambooairways.booking.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<ReviewEntity, String> {
    List<ReviewEntity> findByApprovedTrueOrderByCreatedAtDesc();
}
