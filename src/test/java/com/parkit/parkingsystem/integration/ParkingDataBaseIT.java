package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    private static final String VEHICLE_REG_NUMBER = "ABCDEF";

    @BeforeAll
    private static void setUpBeforeAll() {
        // Configurer les objets DAO
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.setDataBaseConfig(dataBaseTestConfig);
        ticketDAO = new TicketDAO();
        ticketDAO.setDataBaseConfig(dataBaseTestConfig);

        dataBaseTestConfig.clearDataBase(); // Si vous avez une méthode pour vider la base avant chaque test

        // Ajouter une place de parking
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        parkingSpotDAO.save(parkingSpot);

        // Ajouter un ticket pour le véhicule
        Ticket ticket = new Ticket();
        ticket.setVehicleRegNumber(VEHICLE_REG_NUMBER);
        ticket.setParkingSpot(parkingSpot);
        ticket.setInTime(new Date());
        ticketDAO.save(ticket);
    }

    @BeforeEach
    void setUp() throws Exception {

        dataBaseTestConfig.clearDataBase();

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);

        Ticket ticket = new Ticket();
        ticket.setVehicleRegNumber(VEHICLE_REG_NUMBER);
        ticket.setParkingSpot(parkingSpot);
        ticket.setInTime(new Timestamp(System.currentTimeMillis()));
        ticket.setPrice(10.0);
        ticketDAO.saveTicket(ticket);
    }

    @AfterAll
    private static void tearDown() {
        // This method is intentionally left empty because there are no resources to
        // clean up after all tests.
    }

    @Test
    void testParkingACar() throws Exception {
        ParkingSpot parkingSpot = parkingSpotDAO.getNextParkingSpot(ParkingType.CAR);

        assertNotNull(parkingSpot, "La place de parking ne doit pas être nulle");

        Ticket ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber(VEHICLE_REG_NUMBER);
        ticket.setPrice(10.0);
        ticket.setInTime(new Timestamp(System.currentTimeMillis()));
        ticketDAO.saveTicket(ticket);

        Ticket fetchedTicket = ticketDAO.getTicket(VEHICLE_REG_NUMBER);
        assertNotNull(fetchedTicket, "Le ticket ne doit pas être nul après l'insertion dans la base de données");

    }

    @Test
    void testParkingLotExit() throws Exception {
        ParkingSpot parkingSpot = parkingSpotDAO.getNextParkingSpot(ParkingType.CAR);
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber(VEHICLE_REG_NUMBER);
        ticket.setPrice(10.0);
        ticket.setInTime(new Timestamp(System.currentTimeMillis()));
        ticketDAO.saveTicket(ticket);

        Ticket fetchedTicket = ticketDAO.getTicket(VEHICLE_REG_NUMBER);
        assertNotNull(fetchedTicket, "Le ticket ne doit pas être nul après l'insertion dans la base de données");

        fetchedTicket.setOutTime(new Timestamp(System.currentTimeMillis()));
        ticketDAO.updateTicket(fetchedTicket);

        Ticket updatedTicket = ticketDAO.getTicket(VEHICLE_REG_NUMBER);
        assertNotNull(updatedTicket, "Le ticket ne doit toujours pas être nul après la mise à jour");
    }

}
