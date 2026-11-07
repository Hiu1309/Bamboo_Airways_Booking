package com.bambooairways.booking.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(indexes = @Index(columnList = "departureAirportId,arrivalAirportId,departureAt"))
public class FlightEntity {
    @Id public String id;
    @Column(unique = true) public String flightNumber;
    public String departureAirportId;
    public String arrivalAirportId;
    public String aircraftId;
    public Instant departureAt;
    public Instant arrivalAt;
    public int basePrice;
    public int businessPrice;
    @Enumerated(EnumType.STRING) public FlightStatus status = FlightStatus.SCHEDULED;
}
