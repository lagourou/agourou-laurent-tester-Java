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
class ParkingServiceTest {

    @Mock
    private InputReaderUtil inputReaderUtil;
    @Mock
    private ParkingSpotDAO parkingSpotDAO;
    @Mock
    private TicketDAO ticketDAO;
    @InjectMocks
    private ParkingService parkingService;

    private static final String VEHICLE_REG_NUMBER = "ABCDEF";

    @BeforeEach
    private void setUpPerTest() {
        try {
            MockitoAnnotations.openMocks(this);

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
            Ticket ticket = new Ticket();

            ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber(VEHICLE_REG_NUMBER);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    void processExitingVehicleTest() throws Exception {

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setId(1);
        ticket.setVehicleRegNumber(VEHICLE_REG_NUMBER);
        ticket.setParkingSpot(parkingSpot);

        when(ticketDAO.getTicket(VEHICLE_REG_NUMBER)).thenReturn(ticket);
        when(ticketDAO.getNbTicket(VEHICLE_REG_NUMBER)).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VEHICLE_REG_NUMBER);

        parkingService.processExitingVehicle();

        verify(ticketDAO, times(1)).getNbTicket(VEHICLE_REG_NUMBER);
        verify(ticketDAO, times(1)).getTicket(VEHICLE_REG_NUMBER);

    }

    @Test
    void testProcessIncomingVehicle() throws Exception {

        when(ticketDAO.getTicket(VEHICLE_REG_NUMBER)).thenReturn(null);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VEHICLE_REG_NUMBER);

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        when(parkingSpotDAO.getNextParkingSpot(ParkingType.CAR)).thenReturn(parkingSpot);

        parkingService.processIncomingVehicle();

        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
        verify(ticketDAO, times(1)).getTicket(VEHICLE_REG_NUMBER);
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
        verify(parkingSpotDAO, times(1)).getNextParkingSpot(ParkingType.CAR);
    }

    @Test
    void processExitingVehicleTestUnableUpdate() throws Exception {
        ParkingSpot parkingSpot = mock(ParkingSpot.class);

        Ticket ticket = new Ticket();
        ticket.setId(1);
        ticket.setVehicleRegNumber(VEHICLE_REG_NUMBER);
        ticket.setParkingSpot(parkingSpot);

        when(ticketDAO.getNbTicket(VEHICLE_REG_NUMBER)).thenReturn(1);
        when(ticketDAO.getTicket(VEHICLE_REG_NUMBER)).thenReturn(ticket);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VEHICLE_REG_NUMBER);
        lenient().when(ticketDAO.updateTicket(ticket)).thenReturn(false);

        parkingService.processExitingVehicle();

        verify(ticketDAO, times(1)).getNbTicket(VEHICLE_REG_NUMBER);
        verify(ticketDAO, times(1)).getTicket(VEHICLE_REG_NUMBER);
        verify(ticketDAO, times(0)).updateTicket(ticket);

    }

    @Test
    void testGetNextParkingNumberIfAvailable() {

        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

        when(inputReaderUtil.readSelection()).thenReturn(1);

        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        assertNotNull(parkingSpot);
        assertEquals(1, parkingSpot.getId());

        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);
        verify(inputReaderUtil, times(1)).readSelection();
    }

    @Test
    void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {

        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        assertNull(parkingSpot, "La place de parking doit être nulle car aucune place n'est disponible");
    }

    @Test
    void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {

        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(-1);

        when(inputReaderUtil.readSelection()).thenReturn(1);

        ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();
        assertNull(result, "le résultat doit être nulle");

        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);
    }
}