package com.bambooairways.booking.repository;

import com.bambooairways.booking.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByUsernameOrEmail(String username, String email);
    boolean existsByUsernameOrEmail(String username, String email);
}
