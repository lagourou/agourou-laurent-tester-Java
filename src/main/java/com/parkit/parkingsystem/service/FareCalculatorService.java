package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
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

        // TODO: Some tests are failing here. Need to check if this logic is correct

        long duration = outTime - inTime;
        double minutes = duration / (1000.0 * 60);
        double hours = minutes / 60.0;

        if (minutes <= 30) {
            ticket.setPrice(0);
            return;
        }
        double rate;

        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                rate = Fare.CAR_RATE_PER_HOUR;
                ticket.setPrice(hours * rate);
                break;
            }
            case BIKE: {
                rate = Fare.BIKE_RATE_PER_HOUR;
                ticket.setPrice(hours * rate);
                break;
            }
            default:
                throw new IllegalArgumentException("Unkown Parking Type");
        }
        if (discount) {
            ticket.setPrice(ticket.getPrice() * 0.95);
        }
    }

    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }

}