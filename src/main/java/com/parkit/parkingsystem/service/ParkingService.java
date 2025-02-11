package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

public class ParkingService {

    private static final Logger logger = LogManager.getLogger("ParkingService");

    private static FareCalculatorService fareCalculatorService = new FareCalculatorService();

    private InputReaderUtil inputReaderUtil;
    private ParkingSpotDAO parkingSpotDAO;
    private TicketDAO ticketDAO;
    private int count;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    private String vehicleRegNumber;

    public String getVehicleRegNumber() {
        return vehicleRegNumber;
    }

    public void setVehicleRegNumber(String vehicleRegNumber) {
        this.vehicleRegNumber = vehicleRegNumber;
    }

    private boolean discount;

    public boolean discount() {
        return discount;
    }

    public void getDiscount(boolean discount) {
        this.discount = discount;
    }

    public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO) {
        this.inputReaderUtil = inputReaderUtil;
        this.parkingSpotDAO = parkingSpotDAO;
        this.ticketDAO = ticketDAO;

    }

    public void processIncomingVehicle() {
        try {
            ParkingSpot parkingSpot = parkingSpotDAO.getNextParkingSpot(ParkingType.CAR);
            String incomingVehicleRegNumber = inputReaderUtil.readVehicleRegistrationNumber();
            Ticket ticket = ticketDAO.getTicket(incomingVehicleRegNumber);
            int nbTicket = ticketDAO.getNbTicket(incomingVehicleRegNumber);
            boolean ticketDiscount = nbTicket > 0;

            if (parkingSpot != null && parkingSpot.getId() > 0) {
                parkingSpot.setAvailable(false);
                parkingSpotDAO.updateParking(parkingSpot);// allot this parking space and mark it's availability as
                                                          // false

                Date inTime = new Date();
                if (ticket == null) {

                    ticket = new Ticket();
                    ticket.setParkingSpot(parkingSpot);
                    ticket.setVehicleRegNumber(incomingVehicleRegNumber);
                    ticket.setPrice(0);
                    ticket.setInTime(inTime);
                    ticketDAO.saveTicket(ticket);

                    logger.info("Generated Ticket and saved in DB");
                    logger.info("Please park your vehicle in spot number: {}", parkingSpot.getId());
                    logger.info("Recorded in-time for vehicle number: {} is: {}", incomingVehicleRegNumber, inTime);

                }
            }
            if (ticketDiscount) {

                fareCalculatorService.calculateFare(ticket, true);
                logger.info("Glad to see you again");
            } else {

                fareCalculatorService.calculateFare(ticket, false);
                logger.info("Welcome to the parking");
            }
        } catch (Exception e) {
            logger.error("Unable to process incoming vehicle", e);
        }
    }

    public String getVehichleRegNumber() {
        logger.info("Please type the vehicle registration number and press enter key");
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
                logger.info("Next available parking number is: {}", parkingNumber);
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
        logger.info("Please select vehicle type from menu");
        logger.info("1 CAR");
        logger.info("2 BIKE");
        int input = inputReaderUtil.readSelection();
        switch (input) {
            case 1: {
                return ParkingType.CAR;
            }
            case 2: {
                return ParkingType.BIKE;
            }
            default: {
                logger.info("Incorrect input provided");
                throw new IllegalArgumentException("Entered input is invalid");
            }
        }
    }

    public void processExitingVehicle() {
        try {
            String exitingVehicleRegNumber = inputReaderUtil.readVehicleRegistrationNumber();
            Ticket ticket = ticketDAO.getTicket(exitingVehicleRegNumber);
            int ticketCount = ticketDAO.getNbTicket(exitingVehicleRegNumber);

            boolean isUpdated = false;
            if (ticket != null) {
                Date outTime = new Date();
                ticket.setOutTime(outTime);
                fareCalculatorService.calculateFare(ticket, false);
                isUpdated = ticketDAO.updateTicket(ticket);
            }
            if (isUpdated && ticketCount != 0) {
                ParkingSpot parkingSpot = ticket.getParkingSpot();
                parkingSpot.setAvailable(true);
                parkingSpotDAO.updateParking(parkingSpot);

                logger.info("Please park your vehicle in spot number: {}", parkingSpot.getId());
                logger.info("Please pay the parking fare: {}", ticket.getPrice());
                Date outTime = ticket.getOutTime();
                logger.info("Recorded out-time for vehicle number: {} is: {}", ticket.getVehicleRegNumber(), outTime);
            } else {
                logger.info("Unable to update ticket information. Error occurred");
                logger.info("Ticket not found for vehicle number: {}", exitingVehicleRegNumber);
            }
        } catch (Exception e) {
            logger.error("Unable to process exiting vehicle", e);
        }
    }
}
