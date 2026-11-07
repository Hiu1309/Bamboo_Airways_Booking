package com.bambooairways.booking.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.Instant;

@Entity
public class BookingEntity {
    @Id public String id;
    @Column(unique = true) public String bookingCode;
    public String userId;
    public String flightId;
    public String cabinClass;
    public String fareName;
    public String contactEmail;
    public String contactPhone;
    @Enumerated(EnumType.STRING) public BookingStatus status = BookingStatus.PENDING;
    public int subtotal;
    public int tax;
    public int seatFee;
    public int total;
    public Instant expiresAt;
    public Instant createdAt;
    public Instant updatedAt;
}
