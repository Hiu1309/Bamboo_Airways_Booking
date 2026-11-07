package com.bambooairways.booking.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import java.time.Instant;

@Entity
public class PasswordResetTokenEntity {
    @Id public String id;
    @Column(unique = true) public String tokenHash;
    public String userId;
    public Instant expiresAt;
    public Instant usedAt;
    public Instant createdAt;
}
