package com.parkit.parkingsystem;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;

class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;

    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    private void setUpPerTest() {

        fareCalculatorService = new FareCalculatorService();
        ticket = new Ticket();
    }

    @Test
    void calculateFareCarTest() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket.setParkingSpot(parkingSpot);
        ticket.setInTime(new Date());
        ticket.setOutTime(new Date(System.currentTimeMillis() + (60 * 60 * 1000)));

        fareCalculatorService.calculateFare(ticket);

        double expectedFare = 1 * Fare.CAR_RATE_PER_HOUR;
        assertEquals(expectedFare, ticket.getPrice(), 0.001, "Le tarif calculé est incorrect");
    }

    @Test
    void calculateFareBikeTest() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
        ticket.setParkingSpot(parkingSpot);
        ticket.setInTime(new Date());
        ticket.setOutTime(new Date(System.currentTimeMillis() + (60 * 60 * 1000)));

        fareCalculatorService.calculateFare(ticket);

        double expectedFare = 1 * Fare.BIKE_RATE_PER_HOUR;
        assertEquals(expectedFare, ticket.getPrice(), 0.001, "Le tarif calculé est incorrect");
    }

    @Test
    void calculateFareCar() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, false);
        assertEquals(Fare.CAR_RATE_PER_HOUR, ticket.getPrice());
    }

    @Test
    void calculateFareBike() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, false);
        assertEquals(Fare.BIKE_RATE_PER_HOUR, ticket.getPrice());
    }

    @Test
    void calculateFareUnkownType() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, null, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        assertThrows(IllegalArgumentException.class, () -> {
            fareCalculatorService.calculateFare(ticket, false);
        });

    }

    @Test
    void calculateFareBikeWithFutureInTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() + (60 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(IllegalArgumentException.class, () -> {
            fareCalculatorService.calculateFare(ticket, false);
        });
    }

    @Test
    void calculateFareBikeWithLessThanOneHourParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));// 45 minutes parking time should give 3/4th
                                                                      // parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, false);
        assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
    }

    @Test
    void calculateFareCarWithLessThanOneHourParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));// 45 minutes parking time should give 3/4th
                                                                      // parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, false);
        assertEquals((0.75 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
    }

    @Test
    void calculateFareCarWithMoreThanADayParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (24 * 60 * 60 * 1000));// 24 hours parking time should give 24 *
                                                                           // parking fare per hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, false);
        assertEquals((24 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
    }

    @Test
    void calculateFareCarWithLessThan30minutesParkingTime() {
        // Simule le temps d'entrée pour un stationnement de moins de 30 minutes
        Date intTime = new Date();
        intTime.setTime(System.currentTimeMillis() - (30 * 60 * 1000));
        // Initialise l'heure de sortie pour l'heure actuelle
        Date outTime = new Date();

        // Créer une place de parking pour une voiture
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        // Configure le ticket avec les temps d'entrée/sortie et la place de parking
        ticket.setInTime(intTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        // Calcul le tarif sans remise
        fareCalculatorService.calculateFare(ticket, false);
        // Vérifie que le tarif est égal à 0
        assertEquals(0, ticket.getPrice());
    }

    @Test
    void calculateFareBikeWithLessThan30minutesParkingTime() {
        Date intTime = new Date();
        intTime.setTime(System.currentTimeMillis() - (30 * 60 * 1000));

        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(intTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, false);
        assertEquals(0, ticket.getPrice());
    }

    @Test
    void calculateFareCarWithDiscount() {
        // Crée une date d'entrée actuelle
        Date intTime = new Date();

        // Simule le temps de sortie pour un stationnement de 1 heure
        Date outTime = new Date();
        outTime.setTime(System.currentTimeMillis() + (60 * 60 * 1000));

        // Crée une place de parking pour une voiture de ID 1
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        // Crée un ticket pour la voiture
        Ticket discountTicket = new Ticket();
        discountTicket.setInTime(intTime);
        discountTicket.setOutTime(outTime);
        discountTicket.setParkingSpot(parkingSpot);
        // Calcul le tarif avec remise
        fareCalculatorService.calculateFare(discountTicket, true);

        // Vérifie que la remise de 5% est appliqué
        assertEquals(0.95 * Fare.CAR_RATE_PER_HOUR * 1, discountTicket.getPrice(), 0.0001);
    }

    @Test
    void calculateFareBikeWithDiscount() {
        Date intTime = new Date();

        Date outTime = new Date();
        outTime.setTime(System.currentTimeMillis() + (60 * 60 * 1000));

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        Ticket discountTicket = new Ticket();
        discountTicket.setInTime(intTime);
        discountTicket.setOutTime(outTime);
        discountTicket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(discountTicket, true);
        assertEquals(0.95 * Fare.BIKE_RATE_PER_HOUR * 1, discountTicket.getPrice(), 0.0001);
    }
}