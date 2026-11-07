package com.bambooairways.booking.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

@Entity
public class AircraftEntity {
    @Id public String id;
    @Column(unique = true) public String code;
    public String model;
    public String seatLayout = "A,B,C,D,G,H,K";
    public int seatRows = 16;
}
