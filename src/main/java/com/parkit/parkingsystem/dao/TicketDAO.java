package com.parkit.parkingsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;

public class TicketDAO {

    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

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

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (ClassNotFoundException | SQLException ex) {
            return false;
        } finally {
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);
        }
    }

    public boolean saveTicket(Ticket ticket) throws Exception {
        try (Connection con = dataBaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(DBConstants.SAVE_TICKET)) {

            ps.setInt(1, ticket.getParkingSpot().getId());
            ps.setString(2, ticket.getVehicleRegNumber());
            ps.setDouble(3, ticket.getPrice());
            ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
            ps.setTimestamp(5, (ticket.getOutTime() == null) ? null : new Timestamp(ticket.getOutTime().getTime()));
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Ticket inséré avec succès pour le véhicule " + ticket.getVehicleRegNumber());
                return true;
            } else {
                System.out.println("Échec de l'insertion du ticket pour le véhicule " + ticket.getVehicleRegNumber());
                return false;
            }
        } catch (SQLException ex) {
            System.out.println("Erreur lors de l'insertion du ticket");
            ex.printStackTrace();
        }
        return false;
    }

    public Ticket getTicket(String vehicleRegNumber) throws ClassNotFoundException {
        Connection con = null;
        Ticket ticket = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement(DBConstants.GET_TICKET);
            ps.setString(1, vehicleRegNumber);

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
                System.out.println("Aucun ticket trouvé pour le véhicule " + vehicleRegNumber);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur lors de la récupération du ticket pour le véhicule: " + vehicleRegNumber);
            ex.printStackTrace();
        } finally {
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);
        }

        return ticket;
    }

    public int getNbTicket(String vehicleRegNumber) {
        Connection con = null;
        int ticketCount = 0;
        try {
            con = dataBaseConfig.getConnection();
            try (PreparedStatement ps = con.prepareStatement(DBConstants.COUNT_TICKET)) {
                ps.setString(1, vehicleRegNumber);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ticketCount = rs.getInt(1);
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println("Counting error");
            ex.printStackTrace();
        } finally {
            dataBaseConfig.closeConnection(con);
        }
        return ticketCount;
    }

    public boolean updateTicket(Ticket ticket) throws ClassNotFoundException, SQLException {
        Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            try (PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_TICKET)) {
                ps.setDouble(1, ticket.getPrice());
                ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
                ps.setInt(3, ticket.getId());

                int updateRowCount = ps.executeUpdate();
                if (updateRowCount > 0) {
                    System.out.println("Ticket avec l'ID mis à jour avec succès " + ticket.getId());
                    return true;
                } else {
                    System.out.println("Erreur lors de la mise à jour du ticket avec l'ID " + ticket.getId());
                    return false;
                }
            }
        } finally {
            dataBaseConfig.closeConnection(con);
        }
    }
}
