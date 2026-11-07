package com.bambooairways.booking.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;

@Entity
public class PassengerEntity {
    @Id public String id;
    public String bookingId;
    public String title;
    public String firstName;
    public String lastName;
    public String identityNumber;
    public String memberNumber;
    public Instant dateOfBirth;
}
