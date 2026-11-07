package com.bambooairways.booking.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import java.time.Instant;

@Entity
public class LoyaltyEntryEntity {
    @Id public String id;
    @Column(unique = true) public String transactionNo;
    public String userId;
    public String description;
    public String type;
    public int points;
    public Instant createdAt;
}
