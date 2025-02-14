package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    /**
     * @param ticket
     * @param discount
     * 
     */
    public void calculateFare(Ticket ticket, boolean discount) {

        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        long inTime = ticket.getInTime().getTime();
        long outTime = ticket.getOutTime().getTime();

        long duration = outTime - inTime;
        double minutes = duration / (1000.0 * 60);
        double hours = minutes / 60.0;

        if (minutes <= 30) {
            ticket.setPrice(0);
            return;
        }
        ParkingType parkingType = ticket.getParkingSpot().getParkingType();
        if (parkingType == null) {
            throw new IllegalArgumentException("Parking type is unknown");
        }
        double rate;
        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR ->  {
                rate = Fare.CAR_RATE_PER_HOUR;
                ticket.setPrice(hours * rate);
            }
            case BIKE ->  {
                rate = Fare.BIKE_RATE_PER_HOUR;
                ticket.setPrice(hours * rate);
            }
        }
        if (discount) {
            ticket.setPrice(ticket.getPrice() * 0.95);
        }
    }
    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }


}