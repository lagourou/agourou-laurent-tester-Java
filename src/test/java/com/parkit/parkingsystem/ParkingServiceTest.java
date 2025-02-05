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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
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

        } catch (Exception e) {
            throw new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processExitingVehicleTest() throws Exception {

        String vehicleRegNumber = "ABCDEF";
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setId(1);
        ticket.setVehicleRegNumber(vehicleRegNumber);
        ticket.setParkingSpot(parkingSpot);

        when(ticketDAO.getTicket(vehicleRegNumber)).thenReturn(ticket);
        when(ticketDAO.getNbTicket(vehicleRegNumber)).thenReturn(1);
        when(parkingService.getVehichleRegNumber()).thenReturn(vehicleRegNumber);

        parkingService.processExitingVehicle();

        verify(ticketDAO, times(1)).getNbTicket(vehicleRegNumber);
        verify(ticketDAO, times(1)).getTicket(vehicleRegNumber);
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
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        Ticket ticket = new Ticket();
        ticket.setId(1);
        ticket.setVehicleRegNumber(vehicleRegNumber);
        ticket.setParkingSpot(parkingSpot);

        when(ticketDAO.getTicket(vehicleRegNumber)).thenReturn(ticket);
        when(ticketDAO.getNbTicket(vehicleRegNumber)).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(vehicleRegNumber);

        parkingService.processExitingVehicle();

        verify(ticketDAO, times(1)).getTicket(vehicleRegNumber);
        verify(ticketDAO, times(1)).getNbTicket(vehicleRegNumber);

    }

    @Test
    public void testGetNextParkingNumberIfAvailable() throws Exception {

        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

        when(inputReaderUtil.readSelection()).thenReturn(1);

        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        assertNotNull(parkingSpot);
        assertEquals(1, parkingSpot.getId());

        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);
        verify(inputReaderUtil, times(1)).readSelection();
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {

        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        assertNull(parkingSpot, "La place de parking doit être nulle car aucune place n'est disponible");
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() throws Exception {

        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(-1);
        // when(parkingService.getVehichleType()).thenReturn(ParkingType.CAR);
        when(inputReaderUtil.readSelection()).thenReturn(1);

        ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();
        assertNull(result, "le résultat doit être nulle");

        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);
    }
}