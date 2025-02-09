package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class TicketDAO {

    public boolean updateTicket(Ticket ticket) {
        return true;
    }

    private static final Logger logger = LogManager.getLogger("TicketDAO");

    protected DataBaseConfig dataBaseConfig = new DataBaseConfig();

    private int count;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setDataBaseConfig(DataBaseConfig dataBaseConfig) {
        this.dataBaseConfig = dataBaseConfig;
    }

    public boolean save(Ticket ticket) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement(
                    "INSERT INTO ticket (PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME) VALUES (?, ?, ?, ?, ?)");
            ps.setInt(1, ticket.getParkingSpot().getId());
            ps.setString(2, ticket.getVehicleRegNumber());
            ps.setDouble(3, ticket.getPrice());
            ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
            ps.setTimestamp(5, (ticket.getOutTime() == null) ? null : new Timestamp(ticket.getOutTime().getTime()));
            return ps.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);
        }
    }

    public boolean saveTicket(Ticket ticket) throws ClassNotFoundException {
        try (Connection con = dataBaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(DBConstants.SAVE_TICKET)) {

            ps.setInt(1, ticket.getParkingSpot().getId());
            ps.setString(2, ticket.getVehicleRegNumber());
            ps.setDouble(3, ticket.getPrice());
            ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
            ps.setTimestamp(5,
                    (ticket.getOutTime() == null) ? null : (new Timestamp(ticket.getOutTime().getTime())));
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Ticket inséré avec succès pour le véhicule " + ticket.getVehicleRegNumber());
                return true;
            } else {
                logger.warn("Échec de l'insertion du ticket pour le véhicule " + ticket.getVehicleRegNumber());
                return false;
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de l'insertion du ticket", ex);
        }
        return false;
    }

    public Ticket getTicket(String VEHICLE_REG_NUMBER) throws Exception {
        Connection con = null;
        Ticket ticket = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement(DBConstants.GET_TICKET);
            ps.setString(1, VEHICLE_REG_NUMBER);

            rs = ps.executeQuery();

            if (rs.next()) {
                ticket = new Ticket();

                ParkingSpot parkingSpot = new ParkingSpot(
                        rs.getInt("PARKING_NUMBER"),
                        ParkingType.valueOf(rs.getString("TYPE")),
                        false);

                ticket.setId(rs.getInt("ID"));
                ticket.setVehicleRegNumber(rs.getString("VEHICLE_REG_NUMBER"));
                ticket.setPrice(rs.getDouble("PRICE"));
                ticket.setInTime(rs.getTimestamp("IN_TIME"));
                ticket.setOutTime(rs.getTimestamp("OUT_TIME"));
                ticket.setParkingSpot(parkingSpot);
            } else {
                logger.warn("Aucun ticket trouvé pour le véhicule " + VEHICLE_REG_NUMBER);
            }
        } catch (SQLException ex) {
            logger.error("Erreur lors de la récupération du ticket pour le véhicule: " + VEHICLE_REG_NUMBER, ex);
        } finally {
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);
        }

        return ticket;
    }

    public int getNbTicket(String VEHICLE_REG_NUMBER) {
        Connection con = null;
        int ticketCount = 0;
        try {
            con = dataBaseConfig.getConnection();
            try (PreparedStatement ps = con.prepareStatement(DBConstants.COUNT_TICKET)) {
                ps.setString(1, VEHICLE_REG_NUMBER);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        count = rs.getInt(1);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Counting error", ex);
        } finally {
            dataBaseConfig.closeConnection(con);
        }
        return ticketCount;
    }

}
