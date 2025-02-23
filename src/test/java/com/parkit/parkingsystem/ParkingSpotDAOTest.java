package com.parkit.parkingsystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.model.ParkingSpot;

class ParkingSpotDAOTest {

    @Mock
    private DataBaseConfig dataBaseConfig;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private ParkingSpotDAO parkingSpotDAO;

    @BeforeEach
    void setUp() throws SQLException, ClassNotFoundException {
        MockitoAnnotations.openMocks(this);

        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(DBConstants.GET_NEXT_PARKING_SPOT)).thenReturn(preparedStatement);
        when(connection.prepareStatement(DBConstants.UPDATE_PARKING_SPOT)).thenReturn(preparedStatement);
        when(connection.prepareStatement(DBConstants.GET_PARKING_SPOT)).thenReturn(preparedStatement);
    }

    @Test
    void testGetNextAvailableSlot_Success() throws SQLException {
        // Simuler la connexion à la base de données et la requête SQL
        ParkingType parkingType = ParkingType.CAR;

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(5); // ID du prochain parking disponible

        // Appeler la méthode à tester
        int nextAvailableSlot = parkingSpotDAO.getNextAvailableSlot(parkingType);

        // Vérifier que la méthode renvoie correctement l'ID du parking disponible
        assertEquals(5, nextAvailableSlot);

        // Vérifier que la requête a été préparée avec le bon type de stationnement
        verify(preparedStatement).setString(1, parkingType.toString());
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testGetNextAvailableSlot_NoSlotAvailable() throws SQLException {
        // Simuler la situation où aucun emplacement n'est disponible
        ParkingType parkingType = ParkingType.BIKE;

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // Aucun résultat

        // Appeler la méthode à tester
        int nextAvailableSlot = parkingSpotDAO.getNextAvailableSlot(parkingType);

        // Vérifier que la méthode retourne -1 lorsque aucun emplacement n'est
        // disponible
        assertEquals(-1, nextAvailableSlot);
    }

    @Test
    void testGetNextAvailableSlot_ExceptionHandling() throws SQLException {
        // Simuler une exception lors de l'exécution de la requête SQL
        ParkingType parkingType = ParkingType.CAR;

        when(preparedStatement.executeQuery()).thenThrow(new SQLException("Database error"));

        // Appeler la méthode à tester
        int nextAvailableSlot = parkingSpotDAO.getNextAvailableSlot(parkingType);

        // Vérifier que la méthode retourne -1 en cas d'erreur
        assertEquals(-1, nextAvailableSlot);
    }

    @Test
    void testGetNextParkingSpot_Success() throws SQLException {
        // Simuler un emplacement disponible
        ParkingType parkingType = ParkingType.CAR;
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(5); // ID du prochain parking disponible

        // Appeler la méthode à tester
        ParkingSpot nextSpot = parkingSpotDAO.getNextParkingSpot(parkingType);

        // Vérifier que le parking spot retourné est celui attendu
        assertNotNull(nextSpot);
        assertEquals(5, nextSpot.getId());
        assertTrue(nextSpot.isAvailable());
        assertEquals(parkingType, nextSpot.getParkingType());
    }

    @Test
    void testGetNextParkingSpot_NoSpotAvailable() throws SQLException {
        // Simuler la situation où aucun emplacement n'est disponible
        ParkingType parkingType = ParkingType.BIKE;

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // Aucun emplacement trouvé

        // Appeler la méthode
        ParkingSpot nextSpot = parkingSpotDAO.getNextParkingSpot(parkingType);

        // Vérifier que le résultat est nul lorsque aucun emplacement n'est trouvé
        assertNull(nextSpot);
    }

    @Test
    void testUpdateParking_Success() throws SQLException {
        // Créer un objet ParkingSpot à tester
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);

        // Simuler le comportement de la base de données
        when(connection.prepareStatement(DBConstants.UPDATE_PARKING_SPOT)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1); // 1 ligne mise à jour

        // Appeler la méthode à tester
        boolean result = parkingSpotDAO.updateParking(parkingSpot);

        // Vérifier que la méthode retourne true (mise à jour réussie)
        assertTrue(result);

        // Vérifier que la requête préparée est bien configurée avec les bons paramètres
        verify(preparedStatement).setBoolean(1, parkingSpot.isAvailable());
        verify(preparedStatement).setInt(2, parkingSpot.getId());
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdateParking_Failure() throws SQLException {
        // Créer un objet ParkingSpot à tester
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        // Simuler le comportement de la base de données pour échouer
        when(connection.prepareStatement(DBConstants.UPDATE_PARKING_SPOT)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0); // Aucune ligne mise à jour

        // Appeler la méthode à tester
        boolean result = parkingSpotDAO.updateParking(parkingSpot);

        // Vérifier que la méthode retourne false (mise à jour échouée)
        assertFalse(result);

        verify(preparedStatement).setBoolean(1, parkingSpot.isAvailable());
        verify(preparedStatement).setInt(2, parkingSpot.getId());
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdateParking_Exception() throws SQLException {
        // Créer un objet ParkingSpot à tester
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);

        // Simuler une exception SQL
        when(connection.prepareStatement(DBConstants.UPDATE_PARKING_SPOT)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Database error"));

        // Appeler la méthode à tester
        boolean result = parkingSpotDAO.updateParking(parkingSpot);

        // Vérifier que la méthode retourne false en cas d'exception
        assertFalse(result);
    }

    @Test
    void testGetParkingSpot_Success() throws SQLException, ClassNotFoundException {
        // Créer un objet ParkingSpot simulé pour le test
        int parkingSpotId = 1;
        ParkingType parkingType = ParkingType.CAR;
        boolean isAvailable = true;

        // Simuler le comportement de la base de données
        when(connection.prepareStatement(DBConstants.GET_PARKING_SPOT)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        // Utilisez les noms de colonnes corrects ici
        when(resultSet.getString("TYPE")).thenReturn(parkingType.toString());
        when(resultSet.getBoolean("available")).thenReturn(isAvailable);

        // Appeler la méthode à tester
        ParkingSpot result = parkingSpotDAO.getParkingSpot(parkingSpotId);

        // Vérifier que la méthode retourne un objet ParkingSpot avec les bons attributs
        assertNotNull(result, "La place de parking ne doit pas être nulle");
        assertEquals(parkingSpotId, result.getId(), "L'ID de la place de parking doit correspondre");
        assertEquals(parkingType, result.getParkingType(), "Le type de la place de parking doit correspondre");
        assertEquals(isAvailable, result.isAvailable(), "La disponibilité de la place de parking doit correspondre");

        // Vérifier que la requête préparée est configurée correctement
        verify(preparedStatement).setInt(1, parkingSpotId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testGetParkingSpot_NoSpotFound() throws SQLException, ClassNotFoundException {
        // Créer un parking spotId pour le test
        int parkingSpotId = 1;

        // Simuler le comportement de la base de données où aucun résultat n'est trouvé
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // Aucun enregistrement trouvé

        // Appeler la méthode à tester
        ParkingSpot result = parkingSpotDAO.getParkingSpot(parkingSpotId);

        // Vérifier que la méthode retourne null si aucun emplacement n'est trouvé
        assertNull(result);

        // Vérifier que la requête préparée est configurée correctement
        verify(preparedStatement).setInt(1, parkingSpotId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testGetParkingSpot_Exception() throws SQLException, ClassNotFoundException {
        // Créer un parking spotId pour le test
        int parkingSpotId = 1;

        // Simuler une exception SQL lors de l'exécution de la requête
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenThrow(new SQLException("Database error"));

        // Appeler la méthode à tester
        ParkingSpot result = parkingSpotDAO.getParkingSpot(parkingSpotId);

        // Vérifier que la méthode retourne null en cas d'exception
        assertNull(result);
    }
}
