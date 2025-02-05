package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.sql.Date;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    @Mock
    private InputReaderUtil inputReaderUtil;
    @Mock
    private ParkingSpotDAO parkingSpotDAO;
    @Mock
    private TicketDAO ticketDAO;
    @InjectMocks
    private ParkingService parkingService;

    @BeforeEach
    private void setUpPerTest() {
        try {
            MockitoAnnotations.openMocks(this);

            String vehicleRegNumber = "ABCDEF";
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
            Ticket ticket = new Ticket();

            ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber(vehicleRegNumber);

            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(vehicleRegNumber);
            when(ticketDAO.getTicket(vehicleRegNumber)).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
            when(ticketDAO.getNbTicket(vehicleRegNumber)).thenReturn(1);
            when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
            when(parkingSpotDAO.getNextParkingSpot(ParkingType.CAR))
                    .thenReturn(new ParkingSpot(1, ParkingType.CAR, false));

        } catch (Exception e) {
            throw new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processExitingVehicleTest() {

        String vehicleRegNumber = "ABCDEF";
        Ticket ticket = new Ticket();
        ticket.setId(1);
        ticket.getVehicleRegNumber();
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));

        when(ticketDAO.getTicket(vehicleRegNumber)).thenReturn(ticket);
        when(ticketDAO.getNbTicket(vehicleRegNumber)).thenReturn(null);
        when(ticketDAO.updateTicket(ticket)).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class)));

        parkingService.processExitingVehicle();

        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
        verify(ticketDAO, times(1)).getNbTicket(anyString());
        verify(ticketDAO, times(1)).getTicket(anyString());
    }

    @Test
    public void testProcessIncomingVehicle() throws Exception {

        String vehicleRegNumber = "ABCDEF";

        when(ticketDAO.getTicket(vehicleRegNumber)).thenReturn(null);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(vehicleRegNumber);

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        when(parkingSpotDAO.getNextParkingSpot(ParkingType.CAR)).thenReturn(parkingSpot);

        parkingService.processIncomingVehicle();

        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
        verify(ticketDAO, times(1)).getTicket(vehicleRegNumber);
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
        verify(parkingSpotDAO, times(1)).getNextParkingSpot(ParkingType.CAR);
    }

    @Test
    public void processExitingVehicleTestUnableUpdate() throws Exception {
        String vehicleRegNumber = "ABCDEF";
        Ticket ticket = new Ticket();
        ticket.setId(1);
        ticket.setVehicleRegNumber(vehicleRegNumber);
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));

        when(ticketDAO.getTicket(vehicleRegNumber)).thenReturn(ticket);
        when(ticketDAO.getNbTicket(vehicleRegNumber)).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(vehicleRegNumber);

        parkingService.processExitingVehicle();

        verify(ticketDAO, times(1)).getTicket(vehicleRegNumber);
        verify(ticketDAO, times(1)).getNbTicket(vehicleRegNumber);
        verify(ticketDAO, times(1)).updateTicket(ticket);
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
    }

    @Test
    public void testGetNextParkingNumberIfAvailable() {

        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
        when(parkingService.getVehichleType()).thenReturn(ParkingType.CAR);

        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(any(ParkingType.class));
        verify(parkingSpot, times(1));
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {

        parkingService.getNextParkingNumberIfAvailable();

        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(null);
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(any(ParkingType.class));
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {

        parkingService.getNextParkingNumberIfAvailable();
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(null);

        ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

        assertNull(result, "le résultat doit être nulle");

        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(any(ParkingType.class));
    }
}