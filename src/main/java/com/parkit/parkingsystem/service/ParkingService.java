package com.parkit.parkingsystem.service;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;

public class ParkingService {

    private  Logger logger = LogManager.getLogger("ParkingService");

    private FareCalculatorService fareCalculatorService = new FareCalculatorService();
    private static final String VEHICLE_REG_NUMBER = "ABCDEF";

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
        Ticket ticket = ticketDAO.getTicket(VEHICLE_REG_NUMBER);
        int nbTicket = ticketDAO.getNbTicket(VEHICLE_REG_NUMBER);
        boolean ticketDiscount = nbTicket > 0;


        if (parkingSpot != null && parkingSpot.getId() > 0) {
            parkingSpot.setAvailable(false);
            parkingSpotDAO.updateParking(parkingSpot);

            // Si le ticket est null, on le crée
            if (ticket == null) {
                Date inTime = new Date();  // On crée un "inTime" pour le nouveau ticket
                ticket = new Ticket();
                ticket.setParkingSpot(parkingSpot);
                ticket.setVehicleRegNumber(incomingVehicleRegNumber);
                ticket.setPrice(0);
                ticket.setInTime(inTime);
                ticketDAO.saveTicket(ticket);

                System.out.println("Generated Ticket and saved in DB");
                System.out.println("Please park your vehicle in spot number: " + parkingSpot.getId());
                System.out.println("Recorded in-time for vehicle number:"+ VEHICLE_REG_NUMBER +" is:" + inTime);
            } else {
                // Si le ticket existe déjà, on vérifie si "inTime" est null et on l'affecte
                if (ticket.getInTime() == null) {
                    ticket.setInTime(new Date());  // On affecte un "inTime" si nécessaire
                    System.out.println("Updated in-time for existing ticket:" + ticket.getInTime());
                }
                System.out.println("Existing ticket found for vehicle:" + VEHICLE_REG_NUMBER);
            }

            // Log the decision to apply discount
            if (ticketDiscount) {
                System.out.println("Applying discount for vehicle:" + VEHICLE_REG_NUMBER);
                fareCalculatorService.calculateFare(ticket, true);
                System.out.println("Glad to see you again");
            } else {
                fareCalculatorService.calculateFare(ticket, false);
                System.out.println("Welcome to the parking");
            }
        }
    } catch (Exception e) {
        logger.error("Unable to process incoming vehicle", e);
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
                logger.error("Error fetching parking number from DB. Parking slots might be full");
            }
        } catch (IllegalArgumentException ie) {
            logger.error("Error parsing user input for type of vehicle", ie);
        } catch (Exception e) {
            logger.error("Error fetching next available parking slot", e);
        }
        return parkingSpot;
    }

    public ParkingType getVehichleType() {
        System.out.println("Please select vehicle type from menu");
        System.out.println("1 CAR");
        System.out.println("2 BIKE");
        int input = inputReaderUtil.readSelection();
        switch (input) {
            case 1: {
                return ParkingType.CAR;
            }
            case 2: {
                return ParkingType.BIKE;
            }
            default: {
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

                System.out.println("Please park your vehicle in spot number:" + parkingSpot.getId());
                System.out.println("Please pay the parking fare:" + ticket.getPrice());
                System.out.println("Recorded out-time for vehicle number:" +  "is" + ticket.getVehicleRegNumber() + outTime);
            } else {
                System.out.println("Unable to update ticket information. Error occurred");
                System.out.println("Ticket not found for vehicle number:" + exitingVehicleRegNumber);
            }
        } catch (Exception e) {
            logger.error("Unable to process exiting vehicle", e);
        }
    }
}