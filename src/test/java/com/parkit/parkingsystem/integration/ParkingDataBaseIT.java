package com.parkit.parkingsystem.integration;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static DataBasePrepareService dataBasePrepareService;
    private static final String VEHICLE_REG_NUMBER = "ABCDEF";
    private ParkingSpotDAO parkingSpotDAO;
    private TicketDAO ticketDAO;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() {
        dataBaseTestConfig = new DataBaseTestConfig();
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() {

        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();

        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

        dataBasePrepareService.clearDataBaseEntries();

        Mockito.lenient().when(inputReaderUtil.readSelection()).thenReturn(1);
        Mockito.lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
    }

    @AfterAll
    private static void tearDown() {

    }

    @Test
    void testParkingACar() throws Exception {

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        Ticket ticket = ticketDAO.getTicket(VEHICLE_REG_NUMBER);
        assertNotNull(ticket, "Le ticket doit être enregisté dans la base de données");
        assertEquals(VEHICLE_REG_NUMBER, ticket.getVehicleRegNumber());
        assertNotNull(ticket.getInTime());

        ParkingSpot parkingSpot = parkingSpotDAO.getParkingSpot(ticket.getParkingSpot().getId());
        assertNotNull(parkingSpot, "La place de parking  doit être enregisté dans la base de données");
        assertFalse(parkingSpot.isAvailable());
    }

    @Test
    void testParkingLotExit() throws Exception {
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();

        Ticket ticket = ticketDAO.getTicket(VEHICLE_REG_NUMBER);
        assertNotNull(ticket, "Le ticket doit être enregisté dans la base de données");
        assertEquals(VEHICLE_REG_NUMBER, ticket.getVehicleRegNumber());
        assertNotNull(ticket.getOutTime());

        ParkingSpot parkingSpot = parkingSpotDAO.getParkingSpot(ticket.getParkingSpot().getId());
        assertNotNull(parkingSpot, "La place de parking  doit être enregisté dans la base de données");
        assertTrue(parkingSpot.isAvailable());

    }

    @Test
    void testParkingLotExitRecurringUser() {

    }

}