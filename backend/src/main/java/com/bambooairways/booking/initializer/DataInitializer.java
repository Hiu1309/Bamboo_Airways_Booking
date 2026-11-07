package com.bambooairways.booking.initializer;

import com.bambooairways.booking.entity.*;
import com.bambooairways.booking.repository.*;





import org.springframework.boot.CommandLineRunner;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.stereotype.Component;


import java.time.Instant;

import java.time.temporal.ChronoUnit;


/**
 * Tạo dữ liệu mẫu để các thành viên có thể chạy và kiểm tra dự án ngay.
 */
@Component
class DataInitializer implements CommandLineRunner {

    private final UserRepository users;

    private final AirportRepository airports;

    private final AircraftRepository aircraft;

    private final FlightRepository flights;

    private final FareRepository fares;

    private final SeatRepository seats;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    DataInitializer(UserRepository users, AirportRepository airports,
                    AircraftRepository aircraft, FlightRepository flights,
                    FareRepository fares, SeatRepository seats) {
        this.users = users;

        this.airports = airports;

        this.aircraft = aircraft;

        this.flights = flights;

        this.fares = fares;

        this.seats = seats;

    }

    @Override
    public void run(String... args) {
        createSampleUsers();


        AirportEntity hanoi = createAirport(
                "han", "HAN", "Noi Bai International Airport", "Hanoi");

        AirportEntity hoChiMinh = createAirport(
                "sgn", "SGN", "Tan Son Nhat International Airport", "Ho Chi Minh City");

        AircraftEntity airplane = createAircraft();


        if (flights.findByFlightNumber("QH101").isEmpty()) {
            createSampleFlight(hanoi, hoChiMinh, airplane);

        }
    }

    private void createSampleUsers() {
        if (users.findByUsernameOrEmail("admin", "admin").isEmpty()) {
            UserEntity admin = new UserEntity();

            admin.id = "seed-admin";

            admin.username = "admin";

            admin.email = "admin@bamboo.local";

            admin.fullName = "Bamboo Administrator";

            admin.passwordHash = passwordEncoder.encode("Password123!");

            admin.memberNumber = "BAMADMIN";

            admin.role = UserRole.ADMIN;

            users.save(admin);

        }

        if (users.findByUsernameOrEmail("minhanh", "minhanh").isEmpty()) {
            UserEntity customer = new UserEntity();

            customer.id = "seed-customer";

            customer.username = "minhanh";

            customer.email = "minhanh@example.com";

            customer.fullName = "Nguyen Minh Anh";

            customer.passwordHash = passwordEncoder.encode("Password123!");

            customer.memberNumber = "BAM1234567";

            customer.phone = "0901234567";

            users.save(customer);

        }
    }

    private AirportEntity createAirport(String id, String code, String name, String city) {
        return airports.findByCode(code).orElseGet(() -> {
            AirportEntity airport = new AirportEntity();

            airport.id = id;

            airport.code = code;

            airport.name = name;

            airport.city = city;

            return airports.save(airport);

        });

    }

    private AircraftEntity createAircraft() {
        return aircraft.findByCode("A321").orElseGet(() -> {
            AircraftEntity airplane = new AircraftEntity();

            airplane.id = "a321";

            airplane.code = "A321";

            airplane.model = "Airbus A321";

            airplane.seatRows = 16;

            return aircraft.save(airplane);

        });

    }

    private void createSampleFlight(AirportEntity departure, AirportEntity arrival,
                                    AircraftEntity airplane) {
        FlightEntity flight = new FlightEntity();

        flight.id = "qh101";

        flight.flightNumber = "QH101";

        flight.departureAirportId = departure.id;

        flight.arrivalAirportId = arrival.id;

        flight.aircraftId = airplane.id;

        flight.departureAt = Instant.now()
                .plus(2, ChronoUnit.DAYS)
                .truncatedTo(ChronoUnit.DAYS)
                .plus(8, ChronoUnit.HOURS);

        flight.arrivalAt = flight.departureAt.plus(2, ChronoUnit.HOURS);

        flight.basePrice = 1_200_000;

        flight.businessPrice = 2_800_000;

        flight.status = FlightStatus.SCHEDULED;

        flights.save(flight);


        createFare(flight, "ECONOMY", "ECO", flight.basePrice, 112);

        createFare(flight, "BUSINESS", "BUSINESS", flight.businessPrice, 16);

        createSeats(flight);

    }

    private void createFare(FlightEntity flight, String cabinClass, String fareName,
                            int price, int availableSeats) {
        FareEntity fare = new FareEntity();

        fare.id = flight.id + "-" + cabinClass.toLowerCase();

        fare.flightId = flight.id;

        fare.cabinClass = cabinClass;

        fare.fareName = fareName;

        fare.price = price;

        fare.availableSeats = availableSeats;

        fares.save(fare);

    }

    private void createSeats(FlightEntity flight) {
        String[] columns = {"A", "B", "C", "D", "G", "H", "K"};


        for (int row = 1; row <= 16; row++) {
            for (String column : columns) {
                SeatEntity seat = new SeatEntity();

                seat.id = flight.id + "-" + row + column;

                seat.flightId = flight.id;

                seat.code = row + column;

                seat.cabinClass = row <= 3 ? "BUSINESS" : "ECONOMY";

                seat.status = "AVAILABLE";

                seat.price = row <= 3 ? 50_000 : 0;

                seats.save(seat);

            }
        }
    }
}
