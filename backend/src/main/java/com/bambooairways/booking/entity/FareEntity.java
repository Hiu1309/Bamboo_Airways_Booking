package com.bambooairways.booking.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class FareEntity {
    @Id public String id;
    public String flightId;
    public String cabinClass;
    public String fareName;
    public int price;
    public int baggageKg = 7;
    public int availableSeats;
    public boolean refundable;
    public boolean changeable;
}
