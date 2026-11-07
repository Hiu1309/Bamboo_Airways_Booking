package com.bambooairways.booking.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;

@Entity
public class ReviewEntity {
    @Id public String id;
    public String userId;
    public String comment;
    public int rating;
    public boolean approved = true;
    public Instant createdAt;
}
