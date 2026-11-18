package com.bambooairways.booking.initializer;

import com.bambooairways.booking.entity.*;
import com.bambooairways.booking.repository.*;





import org.springframework.boot.CommandLineRunner;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.stereotype.Component;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


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

    private final LoyaltyEntryRepository loyalty;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    DataInitializer(UserRepository users, AirportRepository airports,
                    AircraftRepository aircraft, FlightRepository flights,
                    FareRepository fares, SeatRepository seats,
                    LoyaltyEntryRepository loyalty) {
        this.users = users;

        this.airports = airports;

        this.aircraft = aircraft;

        this.flights = flights;

        this.fares = fares;

        this.seats = seats;

        this.loyalty = loyalty;

    }

    @Override
    public void run(String... args) {
        createSampleUsers();


        AirportEntity hanoi = createAirport(
                "han", "HAN", "Noi Bai International Airport", "Hanoi");

        AirportEntity hoChiMinh = createAirport(
                "sgn", "SGN", "Tan Son Nhat International Airport", "Ho Chi Minh City");

        AirportEntity daNang = createAirport(
                "dad", "DAD", "Da Nang International Airport", "Da Nang");

        AirportEntity nhaTrang = createAirport(
                "cxr", "CXR", "Cam Ranh International Airport", "Nha Trang");

        AircraftEntity airplane = createAircraft();

        if (flights.findByFlightNumber("QH101").isEmpty()) {
            createSampleFlight(hanoi, hoChiMinh, airplane);
        }

        seedRandomFlights(List.of(hanoi, hoChiMinh, daNang, nhaTrang), airplane, 100);
    }

    private void seedRandomFlights(List<AirportEntity> airportList, AircraftEntity airplane, int targetExtra) {
        long existing = flights.count();
        int toCreate = (int) Math.max(0, (targetExtra + 1) - existing);
        if (toCreate == 0) {
            return;
        }

        Random random = new Random(20261118L);
        Instant baseDay = Instant.now().truncatedTo(ChronoUnit.DAYS);
        List<FlightEntity> batchFlights = new ArrayList<>();
        List<FareEntity> batchFares = new ArrayList<>();
        List<SeatEntity> batchSeats = new ArrayList<>();

        for (int i = 1; i <= toCreate; i++) {
            int depIdx = random.nextInt(airportList.size());
            int arrIdx = (depIdx + 1 + random.nextInt(airportList.size() - 1)) % airportList.size();
            AirportEntity departure = airportList.get(depIdx);
            AirportEntity arrival = airportList.get(arrIdx);

            int dayOffset = random.nextInt(14);
            int hour = 5 + random.nextInt(17); // 05:00 - 21:00 UTC
            int minute = random.nextBoolean() ? 0 : 30;
            int durationMinutes = 70 + random.nextInt(60);

            int basePrice = (900 + random.nextInt(16) * 100) * 1000; // 900k - 2.4M
            int businessPrice = basePrice + (1_200 + random.nextInt(10) * 100) * 1000;

            String flightNumber = String.format("QH%03d", 200 + i);
            while (flights.findByFlightNumber(flightNumber).isPresent()) {
                flightNumber = String.format("QH%03d", 200 + i + random.nextInt(700));
            }

            FlightEntity flight = new FlightEntity();
            flight.id = "seed-" + flightNumber.toLowerCase() + "-" + i;
            flight.flightNumber = flightNumber;
            flight.departureAirportId = departure.id;
            flight.arrivalAirportId = arrival.id;
            flight.aircraftId = airplane.id;
            flight.departureAt = baseDay.plus(dayOffset, ChronoUnit.DAYS).plus(hour, ChronoUnit.HOURS).plus(minute, ChronoUnit.MINUTES);
            flight.arrivalAt = flight.departureAt.plus(durationMinutes, ChronoUnit.MINUTES);
            flight.basePrice = basePrice;
            flight.businessPrice = businessPrice;
            flight.status = FlightStatus.SCHEDULED;
            batchFlights.add(flight);

            batchFares.add(buildFare(flight, "ECONOMY", "ECO", basePrice, 112));
            batchFares.add(buildFare(flight, "BUSINESS", "BUSINESS", businessPrice, 16));
            batchSeats.addAll(buildSeats(flight));
        }

        flights.saveAll(batchFlights);
        fares.saveAll(batchFares);
        seats.saveAll(batchSeats);
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

        users.findByUsernameOrEmail("minhanh", "minhanh").ifPresent(customer -> {
            if (loyalty.findByUserIdOrderByCreatedAtDesc(customer.id).isEmpty()) {
                LoyaltyEntryEntity entry = new LoyaltyEntryEntity();
                entry.id = "seed-customer-initial-points";
                entry.userId = customer.id;
                entry.description = "Diem thuong ban dau";
                entry.type = "EARN";
                entry.points = 1000;
                entry.transactionNo = "SEED-LOYALTY-MINHANH";
                entry.createdAt = Instant.now();
                loyalty.save(entry);
            }
        });
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
        fares.save(buildFare(flight, cabinClass, fareName, price, availableSeats));
    }

    private FareEntity buildFare(FlightEntity flight, String cabinClass, String fareName,
                               int price, int availableSeats) {
        FareEntity fare = new FareEntity();
        fare.id = flight.id + "-" + cabinClass.toLowerCase();
        fare.flightId = flight.id;
        fare.cabinClass = cabinClass;
        fare.fareName = fareName;
        fare.price = price;
        fare.availableSeats = availableSeats;
        return fare;
    }

    private void createSeats(FlightEntity flight) {
        seats.saveAll(buildSeats(flight));
    }

    private List<SeatEntity> buildSeats(FlightEntity flight) {
        String[] columns = {"A", "B", "C", "D", "G", "H", "K"};
        List<SeatEntity> result = new ArrayList<>(16 * columns.length);
        for (int row = 1; row <= 16; row++) {
            for (String column : columns) {
                SeatEntity seat = new SeatEntity();
                seat.id = flight.id + "-" + row + column;
                seat.flightId = flight.id;
                seat.code = row + column;
                seat.cabinClass = row <= 3 ? "BUSINESS" : "ECONOMY";
                seat.status = "AVAILABLE";
                seat.price = row <= 3 ? 50_000 : 0;
                result.add(seat);
            }
        }
        return result;
    }
}
