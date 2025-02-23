package com.parkit.parkingsystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;

class TicketDAOTest {

    private TicketDAO ticketDAO;
    private static final String VEHICLE_REG_NUMBER = "ABCDEF";
    private static final Logger logger = LogManager.getLogger(TicketDAOTest.class);

    @Mock
    private DataBaseConfig dataBaseConfig;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;
    @Mock
    private Ticket ticket;

    @BeforeEach
    void setUp() throws ClassNotFoundException, SQLException, IllegalArgumentException, NoSuchFieldException {
        MockitoAnnotations.openMocks(this);
        ticketDAO = new TicketDAO();
        ticketDAO.setDataBaseConfig(dataBaseConfig);

        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(connection.prepareStatement(DBConstants.UPDATE_TICKET)).thenReturn(preparedStatement);
        when(connection.prepareStatement(DBConstants.COUNT_TICKET)).thenReturn(preparedStatement);
        when(connection.prepareStatement(DBConstants.GET_TICKET)).thenReturn(preparedStatement);
        when(connection.prepareStatement(DBConstants.SAVE_TICKET)).thenReturn(preparedStatement);
    }

    @Test
    void testSaveTicket_Success() throws ClassNotFoundException, SQLException {
        ticket = new Ticket();
        ticket.setParkingSpot(new ParkingSpot(1, null, false));
        ticket.setVehicleRegNumber(VEHICLE_REG_NUMBER);
        ticket.setPrice(15.0);
        ticket.setInTime(new Timestamp(System.currentTimeMillis()));

        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = ticketDAO.saveTicket(ticket);

        assertTrue(result);
        verify(dataBaseConfig, times(1)).getConnection();
        verify(connection, times(1)).prepareStatement(any(String.class));
        verify(preparedStatement, times(1)).executeUpdate();
        verify(dataBaseConfig, times(1)).closePreparedStatement(preparedStatement);
        verify(dataBaseConfig, times(1)).closeConnection(connection);
        logger.info("Test SaveTicket_Success Passed");
    }

    @Test
    void testSaveTicket_Failure() throws ClassNotFoundException, SQLException {
        ticket = new Ticket();
        ticket.setParkingSpot(new ParkingSpot(1, null, false));
        ticket.setVehicleRegNumber(VEHICLE_REG_NUMBER);
        ticket.setPrice(15.0);
        ticket.setInTime(new Timestamp(System.currentTimeMillis()));

        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Error saving ticket"));

        boolean result = ticketDAO.saveTicket(ticket);

        assertFalse(result);
        verify(dataBaseConfig, times(1)).getConnection();
        verify(connection, times(1)).prepareStatement(DBConstants.SAVE_TICKET);
        verify(preparedStatement, times(1)).executeUpdate();
        verify(dataBaseConfig, times(1)).closePreparedStatement(preparedStatement);
        verify(dataBaseConfig, times(1)).closeConnection(connection);
        logger.info("Test SaveTicket_Failure Passed");
    }

    @Test
    void testSaveTicket_Exception() throws Exception {
        ticket = new Ticket();
        ticket.setParkingSpot(new ParkingSpot(1, null, false));
        ticket.setVehicleRegNumber(VEHICLE_REG_NUMBER);
        ticket.setPrice(15.0);
        ticket.setInTime(new Timestamp(System.currentTimeMillis()));

        when(preparedStatement.executeUpdate()).thenThrow(new SQLException());

        boolean result = ticketDAO.saveTicket(ticket);

        assertFalse(result);
        verify(dataBaseConfig, times(1)).getConnection();
        verify(connection, times(1)).prepareStatement(DBConstants.SAVE_TICKET);
        verify(preparedStatement, times(1)).executeUpdate();
    }

    @Test
    void testSaveTicket_RowsAffectedGreaterThanZero() throws Exception {
        ticket = new Ticket();
        ticket.setParkingSpot(new ParkingSpot(1, null, false));
        ticket.setVehicleRegNumber(VEHICLE_REG_NUMBER);
        ticket.setPrice(15.0);
        ticket.setInTime(new Timestamp(System.currentTimeMillis()));

        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = ticketDAO.saveTicket(ticket);

        assertTrue(result);
        verify(dataBaseConfig, times(1)).getConnection();
        verify(connection, times(1)).prepareStatement(DBConstants.SAVE_TICKET);
        verify(preparedStatement, times(1)).executeUpdate();
    }

    @Test
    void testSaveTicket_RowsAffectedEqualsZero() throws Exception {
        ticket = new Ticket();
        ticket.setParkingSpot(new ParkingSpot(1, null, false));
        ticket.setVehicleRegNumber(VEHICLE_REG_NUMBER);
        ticket.setPrice(15.0);
        ticket.setInTime(new Timestamp(System.currentTimeMillis()));

        when(preparedStatement.executeUpdate()).thenReturn(0);

        boolean result = ticketDAO.saveTicket(ticket);

        assertFalse(result);
        verify(dataBaseConfig, times(1)).getConnection();
        verify(connection, times(1)).prepareStatement(DBConstants.SAVE_TICKET);
        verify(preparedStatement, times(1)).executeUpdate();
        logger.info("Test SaveTicket_RowsAffectedEqualsZero Passed");

    }

    @Test
    void testGetTicket_Success() throws SQLException, ClassNotFoundException {
        // Simulation de la récupération d'un ticket valide
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("PARKING_NUMBER")).thenReturn(1);
        when(resultSet.getString("TYPE")).thenReturn("CAR");
        when(resultSet.getInt("ID")).thenReturn(1);
        when(resultSet.getString("VEHICLE_REG_NUMBER")).thenReturn("ABCDEF");
        when(resultSet.getDouble("PRICE")).thenReturn(20.0);
        when(resultSet.getTimestamp("IN_TIME")).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(resultSet.getTimestamp("OUT_TIME")).thenReturn(null);

        ticket = ticketDAO.getTicket(VEHICLE_REG_NUMBER);

        assertNotNull(ticket);
        assertEquals(VEHICLE_REG_NUMBER, ticket.getVehicleRegNumber());
        assertEquals(1, ticket.getParkingSpot().getId());
        assertEquals(20.0, ticket.getPrice(), 0.01);
        assertNull(ticket.getOutTime());
    }

    @Test
    void testGetTicket_NotFound() throws SQLException, ClassNotFoundException {
        // Simulation d'un cas où aucun ticket n'est pas trouvé
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        ticket = ticketDAO.getTicket(VEHICLE_REG_NUMBER);
        assertNull(ticket);
    }

    @Test
    void testGetTicket_Exception() throws SQLException, ClassNotFoundException {
        // Simulation d'une erreur lors de la récupération du ticket
        when(preparedStatement.executeQuery()).thenThrow(new SQLException("Erreur de base de données"));

        ticket = ticketDAO.getTicket(VEHICLE_REG_NUMBER);

        assertNull(ticket);

    }

    @Test
    void testGetNbTicket_Success() throws Exception {

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(5);

        int ticketCount = ticketDAO.getNbTicket(VEHICLE_REG_NUMBER);

        assertEquals(5, ticketCount);

        verify(dataBaseConfig, times(1)).getConnection();
        verify(connection, times(1)).prepareStatement(DBConstants.COUNT_TICKET);
        verify(preparedStatement, times(1)).setString(1, VEHICLE_REG_NUMBER);
        verify(preparedStatement, times(1)).executeQuery();
        verify(resultSet, times(1)).next();
        verify(resultSet, times(1)).getInt(1);
    }

    @Test
    void testGetNbTicket_NoResult() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        int ticketCount = ticketDAO.getNbTicket(VEHICLE_REG_NUMBER);

        assertEquals(0, ticketCount);

        verify(dataBaseConfig, times(1)).getConnection();
        verify(connection, times(1)).prepareStatement(DBConstants.COUNT_TICKET);
        verify(preparedStatement, times(1)).setString(1, VEHICLE_REG_NUMBER);
        verify(preparedStatement, times(1)).executeQuery();
        verify(resultSet, times(1)).next();
    }

    @Test
    void testGetNbTicket_Exception() throws Exception {

        when(preparedStatement.executeQuery()).thenThrow(new SQLException("Database error"));

        int ticketCount = ticketDAO.getNbTicket(VEHICLE_REG_NUMBER);

        assertEquals(0, ticketCount);

        verify(dataBaseConfig, times(1)).getConnection();
        verify(connection, times(1)).prepareStatement(DBConstants.COUNT_TICKET);
        verify(preparedStatement, times(1)).setString(1, VEHICLE_REG_NUMBER);
        verify(preparedStatement, times(1)).executeQuery();
    }

    @Test
    void testUpdateTicket_Success() throws Exception {
        // Simuler un comportement où la requête UPDATE réussit
        when(preparedStatement.executeUpdate()).thenReturn(1); // Retourne 1 pour indiquer que 1 ligne a été mise à jour

        Timestamp now = new Timestamp(System.currentTimeMillis());
        when(ticket.getPrice()).thenReturn(10.0);
        when(ticket.getOutTime()).thenReturn(now);
        when(ticket.getId()).thenReturn(1);

        boolean result = ticketDAO.updateTicket(ticket);

        assertTrue(result);
        verify(preparedStatement).setDouble(1, 10.0);

        ArgumentCaptor<Timestamp> timestampCaptor = ArgumentCaptor.forClass(Timestamp.class);
        verify(preparedStatement).setTimestamp(eq(2), timestampCaptor.capture());

        Timestamp capturedTimestamp = timestampCaptor.getValue();

        long tolerance = 1000;
        assertTrue(Math.abs(capturedTimestamp.getTime() - now.getTime()) <= tolerance,
                "La différence entre les timestamps est trop grande");

        verify(preparedStatement).setInt(3, 1);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdateTicket_Failure() throws Exception {
        // Simuler un comportement où la requête UPDATE échoue
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Error updating ticket"));

        Timestamp now = new Timestamp(System.currentTimeMillis());
        when(ticket.getPrice()).thenReturn(10.0);
        when(ticket.getOutTime()).thenReturn(now);
        when(ticket.getId()).thenReturn(1);

        boolean result = ticketDAO.updateTicket(ticket);

        assertFalse(result);
        verify(preparedStatement).setDouble(1, 10.0);

        ArgumentCaptor<Timestamp> timestampCaptor = ArgumentCaptor.forClass(Timestamp.class);
        verify(preparedStatement).setTimestamp(eq(2), timestampCaptor.capture());

        Timestamp capturedTimestamp = timestampCaptor.getValue();

        long tolerance = 1000;
        assertTrue(Math.abs(capturedTimestamp.getTime() - now.getTime()) <= tolerance,
                "La différence entre les timestamps est trop grande");

        verify(preparedStatement).setInt(3, 1);
        verify(preparedStatement).executeUpdate();
    }
}
