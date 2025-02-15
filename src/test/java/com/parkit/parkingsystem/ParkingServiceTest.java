package com.parkit.parkingsystem;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;


@ExtendWith(MockitoExtension.class)


class ParkingServiceTest {

    @Mock
    private InputReaderUtil inputReaderUtil;
    @Mock
    private ParkingSpotDAO parkingSpotDAO;
    @Mock
    private TicketDAO ticketDAO;
    @Mock
    private FareCalculatorService fareCalculatorService;

    @InjectMocks
    private ParkingService parkingService;

    private static final String VEHICLE_REG_NUMBER = "ABCDEF";
    private ParkingSpot parkingSpot;
    private Ticket ticket;

    @BeforeEach
    public void setUpPerTest() {
        MockitoAnnotations.openMocks(this);

        parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        ticket = new Ticket();

        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber(VEHICLE_REG_NUMBER);
    }

    @Test
    void testProcessIncomingVehicle() throws Exception {

        when(ticketDAO.getTicket(VEHICLE_REG_NUMBER)).thenReturn(null);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VEHICLE_REG_NUMBER);

        parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        when(parkingSpotDAO.getNextParkingSpot(ParkingType.CAR)).thenReturn(parkingSpot);

        parkingService.processIncomingVehicle();

        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
        verify(ticketDAO, times(1)).getTicket(VEHICLE_REG_NUMBER);
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
        verify(parkingSpotDAO, times(1)).getNextParkingSpot(ParkingType.CAR);
    }
    @Test
    void testProcessIncomingVehicle_WithDiscount() throws Exception {

        Ticket mockTicket = new Ticket();
        mockTicket.setVehicleRegNumber(VEHICLE_REG_NUMBER);
        mockTicket.setInTime(new Date(0));
    
        when(ticketDAO.getTicket(VEHICLE_REG_NUMBER)).thenReturn(mockTicket);
        when(ticketDAO.getNbTicket(VEHICLE_REG_NUMBER)).thenReturn(1);
        doNothing().when(fareCalculatorService).calculateFare(any(Ticket.class), eq(true));
    
        parkingService.setFareCalculatorService(fareCalculatorService);
    
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VEHICLE_REG_NUMBER);
    
        parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        when(parkingSpotDAO.getNextParkingSpot(ParkingType.CAR)).thenReturn(parkingSpot);
    
        parkingService.processIncomingVehicle();
    
        verify(ticketDAO, times(0)).saveTicket(any(Ticket.class));
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
    
        assertNotNull(mockTicket.getInTime(), "Ticket should have a valid in-time");
        assertEquals(VEHICLE_REG_NUMBER, mockTicket.getVehicleRegNumber(), "Vehicle registration number should match");

        verify(fareCalculatorService, times(1)).calculateFare(any(Ticket.class), eq(true));
    }
    @Test
    void testProcessIncomingVehicle_WithoutDiscount() throws Exception {

        Ticket mockTicket = new Ticket();
        mockTicket.setVehicleRegNumber(VEHICLE_REG_NUMBER);
        mockTicket.setInTime(new Date(0));
    
        when(ticketDAO.getTicket(VEHICLE_REG_NUMBER)).thenReturn(mockTicket);
        when(ticketDAO.getNbTicket(VEHICLE_REG_NUMBER)).thenReturn(0);
        doNothing().when(fareCalculatorService).calculateFare(any(Ticket.class), eq(false));
    
        parkingService.setFareCalculatorService(fareCalculatorService);
    
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VEHICLE_REG_NUMBER);
    
        parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        when(parkingSpotDAO.getNextParkingSpot(ParkingType.CAR)).thenReturn(parkingSpot);
    
        parkingService.processIncomingVehicle();
    
        verify(ticketDAO, times(0)).saveTicket(any(Ticket.class));
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
    
        assertNotNull(mockTicket.getInTime(), "Ticket should have a valid in-time");
        assertEquals(VEHICLE_REG_NUMBER, mockTicket.getVehicleRegNumber(), "Vehicle registration number should match");

        verify(fareCalculatorService, times(1)).calculateFare(any(Ticket.class), eq(false));
    }
    
    @Test
    void testGetVehichleRegNumber() {

        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VEHICLE_REG_NUMBER);

        assertEquals(VEHICLE_REG_NUMBER, parkingService.getVehichleRegNumber());

        verify(inputReaderUtil, times(1)).readVehicleRegistrationNumber();
        
    }

    @Test
    void testGetNextParkingNumberIfAvailable() {

        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

        when(inputReaderUtil.readSelection()).thenReturn(1);

        parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        assertNotNull(parkingSpot);
        assertEquals(1, parkingSpot.getId());

        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);
        verify(inputReaderUtil, times(1)).readSelection();
    }
    @Test
    void testGetNextParkingNumberIfAvailableException() {
        InputReaderUtil mockInputReader = mock(InputReaderUtil.class);

        when(mockInputReader.readSelection()).thenReturn(3);

        ParkingSpotDAO mockParkingSpotDAO = mock(ParkingSpotDAO.class);
        TicketDAO mockTicketDAO = mock(TicketDAO.class);
        when(mockParkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1); // Retourne un parking valide
        
        parkingService = new ParkingService(mockInputReader, mockParkingSpotDAO, mockTicketDAO);
        parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        
        assertNull(parkingSpot, "La place de parking doit être nulle après une IllegalArgumentException");
        when(mockInputReader.readSelection()).thenReturn(1); // Voiture valide
        when(mockParkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenThrow
        (new RuntimeException("Error fetching next available parking slot"));
        
        parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        assertNull(parkingSpot, "La place de parking doit être nulle après une RuntimeException");
    }
    
    @Test
    void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(-1);

        when(inputReaderUtil.readSelection()).thenReturn(1);

        parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        assertNull(parkingSpot, "La place de parking doit être nulle car aucune place n'est disponible");

        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);
    }

    @Test
    void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {

        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(-1);

        when(inputReaderUtil.readSelection()).thenReturn(1);

        ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();
        assertNull(result, "le résultat doit être nulle");

        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);
    }

    @Test
    void testgetVehichleTypeCar() {
        when(inputReaderUtil.readSelection()).thenReturn(1);

        ParkingType parkingType = parkingService.getVehichleType();

        assertEquals(ParkingType.CAR, parkingType);

        verify(inputReaderUtil, times(1)).readSelection();
    }

    @Test
    void testgetVehichleTypeBike() {
        when(inputReaderUtil.readSelection()).thenReturn(2);

        ParkingType parkingType = parkingService.getVehichleType();

        assertEquals(ParkingType.BIKE, parkingType);

        verify(inputReaderUtil, times(1)).readSelection();
    }

    @Test
    void processExitingVehicleTest() throws Exception {

        parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket = new Ticket();
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
    void processExitingVehicleTestUnableUpdate() throws Exception {
        parkingSpot = mock(ParkingSpot.class);

        ticket = new Ticket();
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
    void processExitingVehicleSucessfulUpdate() throws Exception {

        when(ticketDAO.getNbTicket(VEHICLE_REG_NUMBER)).thenReturn(1);
        when(ticketDAO.getTicket(VEHICLE_REG_NUMBER)).thenReturn(ticket);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VEHICLE_REG_NUMBER);

        doReturn(true).when(ticketDAO).updateTicket(any(Ticket.class));

        parkingService.processExitingVehicle();

        verify(ticketDAO, times(1)).updateTicket(ticket);

    }
}