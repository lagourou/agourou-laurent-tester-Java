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
import static org.mockito.ArgumentMatchers.any;
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
    void setUp() throws Exception, SQLException {
        // Initialise les mocks
        MockitoAnnotations.openMocks(this);
        // Initialise l'objet ticketDAO
        ticketDAO = new TicketDAO();
        ticketDAO.setDataBaseConfig(dataBaseConfig);

        // Simule la connection à la base de données et la préparation des requêtes SQL
        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(connection.prepareStatement(DBConstants.UPDATE_TICKET)).thenReturn(preparedStatement);
        when(connection.prepareStatement(DBConstants.COUNT_TICKET)).thenReturn(preparedStatement);
        when(connection.prepareStatement(DBConstants.GET_TICKET)).thenReturn(preparedStatement);
        when(connection.prepareStatement(DBConstants.SAVE_TICKET)).thenReturn(preparedStatement);
    }

    @Test
    void testSaveTicket_Success() throws Exception, SQLException {
        // Créer un nouveau ticket avec des valeurs spécifiques
        ticket = new Ticket();
        ticket.setParkingSpot(new ParkingSpot(1, null, false));
        ticket.setVehicleRegNumber(VEHICLE_REG_NUMBER);
        ticket.setPrice(15.0);
        ticket.setInTime(new Timestamp(System.currentTimeMillis()));

        // Simule l'exécution de la requete UPADTE pour qu'elle retourne 1: mise à jour
        // résussi
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Appel de la méthode saveTicket du ticketDAO
        boolean result = ticketDAO.saveTicket(ticket);

        // Vérifie que le résultat est vrai
        assertTrue(result);
        // Vérifie que les méthodes attendues on été appelées une fois
        verify(dataBaseConfig, times(1)).getConnection();
        verify(connection, times(1)).prepareStatement(any(String.class));
        verify(preparedStatement, times(1)).executeUpdate();
        verify(dataBaseConfig, times(1)).closePreparedStatement(preparedStatement);
        verify(dataBaseConfig, times(1)).closeConnection(connection);
    }

    @Test
    void testSaveTicket_Failure() throws Exception, SQLException {
        // Créer un nouveau ticket avec des valeurs spécifiques
        ticket = new Ticket();
        ticket.setParkingSpot(new ParkingSpot(1, null, false));
        ticket.setVehicleRegNumber(VEHICLE_REG_NUMBER);
        ticket.setPrice(15.0);
        ticket.setInTime(new Timestamp(System.currentTimeMillis()));

        // Simule l'exécution de la requete UPADTE pour qu'elle lance une exception:
        // mise à jour à échoué
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Error saving ticket"));

        // Appel de la méthode saveTicket du ticketDAO
        boolean result = ticketDAO.saveTicket(ticket);

        // Vérifie que le résultat est faux
        assertFalse(result);

        // Vérifie que les méthodes attendues on été appelées une fois
        verify(dataBaseConfig, times(1)).getConnection();
        verify(connection, times(1)).prepareStatement(DBConstants.SAVE_TICKET);
        verify(preparedStatement, times(1)).executeUpdate();
        verify(dataBaseConfig, times(1)).closePreparedStatement(preparedStatement);
        verify(dataBaseConfig, times(1)).closeConnection(connection);
    }

    @Test
    void testSaveTicket_Exception() throws Exception {
        // Créer un nouveau ticket avec des valeurs spécifiques
        ticket = new Ticket();
        ticket.setParkingSpot(new ParkingSpot(1, null, false));
        ticket.setVehicleRegNumber(VEHICLE_REG_NUMBER);
        ticket.setPrice(15.0);
        ticket.setInTime(new Timestamp(System.currentTimeMillis()));

        // Simule que l'exécution de la requête UPDATE lance une SQLException
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException());

        // Appel de la méthode saveTicket du ticketDAO
        boolean result = ticketDAO.saveTicket(ticket);

        // Vérifie que le résultat est faux: sauvegarde échouée
        assertFalse(result);
        // Vérifie que les méthodes attendues on été appelées une fois
        verify(dataBaseConfig, times(1)).getConnection();
        verify(connection, times(1)).prepareStatement(DBConstants.SAVE_TICKET);
        verify(preparedStatement, times(1)).executeUpdate();
    }

    // Test de le méthode saveTicket lorsque le nombre de lignes est supèrieur à 0
    @Test
    void testSaveTicket_RowsAffectedGreaterThanZero() throws Exception {
        // Créer un nouveau ticket avec des valeurs spécifiques
        ticket = new Ticket();
        ticket.setParkingSpot(new ParkingSpot(1, null, false));
        ticket.setVehicleRegNumber(VEHICLE_REG_NUMBER);
        ticket.setPrice(15.0);
        ticket.setInTime(new Timestamp(System.currentTimeMillis()));

        // Simule que 1 ligne à été affectée par la requête
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Appel de la méthode saveTicket
        boolean result = ticketDAO.saveTicket(ticket);

        // Vérifie que la méthode renvoie true
        assertTrue(result);
        verify(dataBaseConfig, times(1)).getConnection();
        verify(connection, times(1)).prepareStatement(DBConstants.SAVE_TICKET);
        verify(preparedStatement, times(1)).executeUpdate();
    }

    // Test de le méthode saveTicket lorsque le nombre de lignes est égale à 0
    @Test
    void testSaveTicket_RowsAffectedEqualsZero() throws Exception {
        // Créer un nouveau ticket avec des valeurs spécifiques
        ticket = new Ticket();
        ticket.setParkingSpot(new ParkingSpot(1, null, false));
        ticket.setVehicleRegNumber(VEHICLE_REG_NUMBER);
        ticket.setPrice(15.0);
        ticket.setInTime(new Timestamp(System.currentTimeMillis()));

        // Simule que 0 ligne à été affectée par la requête
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // Appel de la méthode saveTicket
        boolean result = ticketDAO.saveTicket(ticket);

        // Vérifie que la méthode renvoie false
        assertFalse(result);
        verify(dataBaseConfig, times(1)).getConnection();
        verify(connection, times(1)).prepareStatement(DBConstants.SAVE_TICKET);
        verify(preparedStatement, times(1)).executeUpdate();

    }

    // Test de la méthode getTicket pour un ticket valide
    @Test
    void testGetTicket_Success() throws Exception, SQLException {
        // Simulation de la récupération d'un ticket valide avec des valeurs spécifiques
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("PARKING_NUMBER")).thenReturn(1);
        when(resultSet.getString("TYPE")).thenReturn("CAR");
        when(resultSet.getInt("ID")).thenReturn(1);
        when(resultSet.getString("VEHICLE_REG_NUMBER")).thenReturn("ABCDEF");
        when(resultSet.getDouble("PRICE")).thenReturn(20.0);
        when(resultSet.getTimestamp("IN_TIME")).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(resultSet.getTimestamp("OUT_TIME")).thenReturn(null);

        // Vérifie que le ticket récupéré est validé
        ticket = ticketDAO.getTicket(VEHICLE_REG_NUMBER);

        // Vérifie que le ticket n'est pas nul
        assertNotNull(ticket);
        assertEquals(VEHICLE_REG_NUMBER, ticket.getVehicleRegNumber());
        assertEquals(1, ticket.getParkingSpot().getId());
        assertEquals(20.0, ticket.getPrice(), 0.01);
        assertNull(ticket.getOutTime());
    }

    // Test de la méthode getTicket pour un ticket non trouvé
    @Test
    void testGetTicket_NotFound() throws Exception, SQLException {
        // Simulation d'un cas où aucun ticket n'est pas trouvé
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // Vérifie que le ticket récupéré est validé
        ticket = ticketDAO.getTicket(VEHICLE_REG_NUMBER);
        // Vérifie que le ticket récupéré est validé mais il est nul
        assertNull(ticket);
    }

    // Test de la méthode getTicket pour un ticket trouvé mais il y a une erreur
    @Test
    void testGetTicket_Exception() throws Exception, SQLException {
        // Simulation d'une erreur de base de donnée lors de la récupération du ticket
        when(preparedStatement.executeQuery()).thenThrow(new SQLException("Database error"));

        // Vérifie que le ticket récupéré est validé
        ticket = ticketDAO.getTicket(VEHICLE_REG_NUMBER);
        // Vérifie que le ticket récupéré est validé mais il est nul
        assertNull(ticket);
    }

    // Test de la méthode getNbTicket pour un ticket valide
    @Test
    void testGetNbTicket_Success() throws Exception {
        // Simulation de la requête réussi avec 5 tickets
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(5);

        // Appel de la méthode getNbTicket
        int ticketCount = ticketDAO.getNbTicket(VEHICLE_REG_NUMBER);

        // Vérifie que le nombre de tiket est correct
        assertEquals(5, ticketCount);
        // Vérifie que les méthodes ont été appelée une fois
        verify(dataBaseConfig, times(1)).getConnection();
        verify(connection, times(1)).prepareStatement(DBConstants.COUNT_TICKET);
        verify(preparedStatement, times(1)).setString(1, VEHICLE_REG_NUMBER);
        verify(preparedStatement, times(1)).executeQuery();
        verify(resultSet, times(1)).next();
        verify(resultSet, times(1)).getInt(1);
    }

    @Test
    void testGetNbTicket_NoResult() throws Exception {
        // Simulation de la requête réussi mais sans résultat
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // Appel de la méthode getNbTicket
        int ticketCount = ticketDAO.getNbTicket(VEHICLE_REG_NUMBER);

        // Vérifie que le nombre de tiket est 0
        assertEquals(0, ticketCount);
        // Vérifie que les méthodes ont été appelée une fois
        verify(dataBaseConfig, times(1)).getConnection();
        verify(connection, times(1)).prepareStatement(DBConstants.COUNT_TICKET);
        verify(preparedStatement, times(1)).setString(1, VEHICLE_REG_NUMBER);
        verify(preparedStatement, times(1)).executeQuery();
        verify(resultSet, times(1)).next();
    }

    @Test
    void testGetNbTicket_Exception() throws Exception {
        // Simule une exception lors de l'éxécution de la requête
        when(preparedStatement.executeQuery()).thenThrow(new SQLException("Database error"));

        // Appel de la méthode getNbTicket
        int ticketCount = ticketDAO.getNbTicket(VEHICLE_REG_NUMBER);

        // Vérifie que le nombre de tiket est 0 en cas d'erreur
        assertEquals(0, ticketCount);
        // Vérifie que les méthodes ont été appelée une fois
        verify(dataBaseConfig, times(1)).getConnection();
        verify(connection, times(1)).prepareStatement(DBConstants.COUNT_TICKET);
        verify(preparedStatement, times(1)).setString(1, VEHICLE_REG_NUMBER);
        verify(preparedStatement, times(1)).executeQuery();
    }
}
