package com.bambooairways.booking.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(indexes = @Index(columnList = "flightId,status"))
public class SeatEntity {
    @Id public String id;
    public String flightId;
    public String code;
    public String cabinClass;
    public String status = "AVAILABLE";
    public int price;
    public String bookingId;
}
