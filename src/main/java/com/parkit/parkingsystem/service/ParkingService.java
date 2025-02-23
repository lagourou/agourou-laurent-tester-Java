package com.parkit.parkingsystem.service;

import java.util.Date;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;

public class ParkingService {

    private FareCalculatorService fareCalculatorService = new FareCalculatorService();

    private final InputReaderUtil inputReaderUtil;
    private final ParkingSpotDAO parkingSpotDAO;
    private final TicketDAO ticketDAO;

    public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO) {
        this.inputReaderUtil = inputReaderUtil;
        this.parkingSpotDAO = parkingSpotDAO;
        this.ticketDAO = ticketDAO;
    }

    public void setFareCalculatorService(FareCalculatorService fareCalculatorService) {
        this.fareCalculatorService = fareCalculatorService;
    }

    public void processIncomingVehicle() {
        try {
            ParkingSpot parkingSpot = parkingSpotDAO.getNextParkingSpot(ParkingType.CAR);
            String incomingVehicleRegNumber = inputReaderUtil.readVehicleRegistrationNumber();
            System.out.println("Processing vehicle with registration: " + incomingVehicleRegNumber);
            Ticket ticket = ticketDAO.getTicket(incomingVehicleRegNumber);
            int nbTicket = ticketDAO.getNbTicket(incomingVehicleRegNumber);
            boolean ticketDiscount = nbTicket > 0;

            if (parkingSpot != null && parkingSpot.getId() > 0) {
                parkingSpot.setAvailable(false);
                parkingSpotDAO.updateParking(parkingSpot);

                if (ticket == null) {
                    Date inTime = new Date();
                    ticket = new Ticket();
                    ticket.setParkingSpot(parkingSpot);
                    ticket.setVehicleRegNumber(incomingVehicleRegNumber);
                    ticket.setPrice(0);
                    ticket.setInTime(inTime);
                    ticketDAO.saveTicket(ticket);

                    System.out.println("Generated Ticket and saved in DB");
                    System.out.println("Please park your vehicle in spot number: " + parkingSpot.getId());
                    System.out.println(
                            "Recorded in-time for vehicle number:" + incomingVehicleRegNumber + " is:" + inTime);
                }
                if (ticketDiscount) {
                    System.out.println("Applying discount for vehicle:" + incomingVehicleRegNumber);
                    fareCalculatorService.calculateFare(ticket, true);
                    System.out.println("Glad to see you again");
                } else {
                    fareCalculatorService.calculateFare(ticket, false);
                    System.out.println("Welcome to the parking");
                }
            }
        } catch (Exception e) {
        }
    }

    public String getVehichleRegNumber() {
        System.out.println("Please type the vehicle registration number and press enter key");
        return inputReaderUtil.readVehicleRegistrationNumber();
    }

    public ParkingSpot getNextParkingNumberIfAvailable() {
        int parkingNumber = 0;
        ParkingSpot parkingSpot = null;
        try {
            ParkingType parkingType = getVehichleType();
            parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
            if (parkingNumber > 0) {
                parkingSpot = new ParkingSpot(parkingNumber, parkingType, true);
                System.out.println("Next available parking number is:" + parkingNumber);
            } else {
                System.err.println("Error fetching parking number from DB. Parking slots might be full");
            }
        } catch (IllegalArgumentException ie) {
            System.err.println("Error parsing user input for type of vehicle");
        } catch (Exception e) {
            System.err.println("Error fetching next available parking slot");
        }
        return parkingSpot;
    }

    public ParkingType getVehichleType() {
        System.out.println("Please select vehicle type from menu");
        System.out.println("1 CAR");
        System.out.println("2 BIKE");
        int input = inputReaderUtil.readSelection();
        switch (input) {
            case 1 -> {
                return ParkingType.CAR;
            }
            case 2 -> {
                return ParkingType.BIKE;
            }
            default -> {
                System.out.println("Incorrect input provided");
                throw new IllegalArgumentException("Entered input is invalid");
            }
        }
    }

    public void processExitingVehicle() {
        try {
            String exitingVehicleRegNumber = inputReaderUtil.readVehicleRegistrationNumber();
            Ticket ticket = ticketDAO.getTicket(exitingVehicleRegNumber);
            int ticketCount = ticketDAO.getNbTicket(exitingVehicleRegNumber);
            Date outTime = null;

            boolean isUpdated = false;
            if (ticket != null) {
                outTime = new Date();
                ticket.setOutTime(outTime);
                fareCalculatorService.calculateFare(ticket, false);
                isUpdated = ticketDAO.updateTicket(ticket);
            }
            if (isUpdated && ticketCount != 0) {
                ParkingSpot parkingSpot = ticket.getParkingSpot();
                parkingSpot.setAvailable(true);
                parkingSpotDAO.updateParking(parkingSpot);

                System.out.println("Please pay the parking fare:" + ticket.getPrice());
                System.out.println(
                        "Recorded out-time for vehicle number:" + "is" + ticket.getVehicleRegNumber() + outTime);
            }
        } catch (Exception e) {
            System.err.println("Unable to process exiting vehicle");
        }
    }
}
