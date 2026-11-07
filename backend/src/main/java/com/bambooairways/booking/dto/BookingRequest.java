package com.bambooairways.booking.dto;

public record BookingRequest(
        String flightId,
        String cabinClass,
        String fareName,
        String contactEmail,
        String contactPhone,
        String seatCode,
        PassengerRequest passenger) {

    public record PassengerRequest(
            String title,
            String firstName,
            String lastName,
            String identityNumber,
            String memberNumber,
            String dateOfBirth) {
    }
}
