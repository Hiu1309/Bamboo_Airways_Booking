package com.bambooairways.booking.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

@Entity
public class AirportEntity {
    @Id public String id;
    @Column(unique = true) public String code;
    public String name;
    public String city;
    public String country = "Vietnam";
}
