package com.parkit.parkingsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;

public class TicketDAO {

    private static final Logger logger = LogManager.getLogger("TicketDAO");

    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    public void setDataBaseConfig(DataBaseConfig dataBaseConfig) {
        this.dataBaseConfig = dataBaseConfig;
    }

    public boolean saveTicket(Ticket ticket) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement(DBConstants.SAVE_TICKET); // PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME,
                                                                // OUT_TIME
            ps.setInt(1, ticket.getParkingSpot().getId());
            ps.setString(2, ticket.getVehicleRegNumber());
            ps.setDouble(3, ticket.getPrice());
            ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
            ps.setTimestamp(5, (ticket.getOutTime() == null) ? null : (new Timestamp(ticket.getOutTime().getTime())));

            int result = ps.executeUpdate();
            return result == 1;
        } catch (SQLException e) {
            logger.error("Erreur SQL lors de l'enregistrement du ticket", e);
        } catch (Exception ex) {
            logger.error("Erreur de sauvegarde du ticket", ex);
        } finally {
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);
        }
        return false;
    }

    public Ticket getTicket(String vehicleRegNumber) {
        Connection con = null;
        Ticket ticket = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.GET_TICKET); // PARKING_NUMBER,
                                                                                 // ID,VEHICLE_REG_NUMBER, PRICE,
                                                                                 // IN_TIME,OUT_TIME, TYPE
            ps.setString(1, vehicleRegNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ticket = new Ticket();
                ParkingSpot parkingSpot = new ParkingSpot(rs.getInt("PARKING_NUMBER"),
                        ParkingType.valueOf(rs.getString("TYPE")), false);
                ticket.setParkingSpot(parkingSpot);
                ticket.setId(rs.getInt("ID"));
                ticket.setVehicleRegNumber(rs.getString("VEHICLE_REG_NUMBER"));
                ticket.setPrice(rs.getDouble("PRICE"));
                ticket.setInTime(rs.getTimestamp("IN_TIME"));
                ticket.setOutTime(rs.getTimestamp("OUT_TIME"));
            } else {
                logger.info("Aucun ticket trouvé pour le véhicule: " + vehicleRegNumber);
            }
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
        } catch (Exception ex) {
            logger.error("Erreur lors de la récupération du prochain emplacement disponible", ex);
        } finally {
            dataBaseConfig.closeConnection(con);
        }
        return ticket;
    }

    public boolean updateTicket(Ticket ticket) throws Exception {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement(DBConstants.UPDATE_TICKET); // PRICE, OUT_TIME, ID
            ps.setDouble(1, ticket.getPrice());
            ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
            ps.setInt(3, ticket.getId());

            int result = ps.executeUpdate();

            if (result == 1) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            logger.error("Erreur SQL lors de la mise à jour du ticket", e);
            return false;
        } catch (Exception ex) {
            logger.error("Erreur lors de la mise à jour du ticket", ex);
            return false;
        } finally {
            if (ps != null) {
                dataBaseConfig.closePreparedStatement(ps);
            }
            if (con != null) {
                dataBaseConfig.closeConnection(con);
            }
        }
    }

    // Méthode pour obtenir le nbre de tickets pour un véhicule donné
    public int getNbTicket(String vehicleRegNumber) throws Exception {
        Connection con = null; // Initialisation de la connexion à la base de données
        int ticketCount = 0; // Initialisation du compteur
        try {
            con = dataBaseConfig.getConnection(); // Connexion à la base de données
            try (PreparedStatement ps = con.prepareStatement(DBConstants.COUNT_TICKET)) { // Préparation de la requête
                                                                                          // SQL
                ps.setString(1, vehicleRegNumber); // Remplace le paramètre par la plaque
                try (ResultSet rs = ps.executeQuery()) { // Exécute la requête
                    if (rs.next()) { // Vérifie si le résultat contient une ligne
                        ticketCount = rs.getInt(1); // Récupère le nbre de tickets
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors du comptage des tickets pour le véhicule: " + vehicleRegNumber, ex);
        } finally {
            dataBaseConfig.closeConnection(con); // Ferme la connexion
        }
        return ticketCount; // Retourne le nbre de tickets
    }
}
