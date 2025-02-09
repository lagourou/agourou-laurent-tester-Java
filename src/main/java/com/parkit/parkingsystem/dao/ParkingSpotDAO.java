package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ParkingSpotDAO {
    private static final Logger logger = LogManager.getLogger("ParkingSpotDAO");

    protected DataBaseConfig dataBaseConfig = new DataBaseConfig();

    public void save(ParkingSpot parkingSpot) {
        // Implementation to save the parking spot to the database
    }

    public void setDataBaseConfig(DataBaseConfig dataBaseConfig) {
        this.dataBaseConfig = dataBaseConfig;
    }

    public int getNextAvailableSlot(ParkingType parkingType) {
        int result = -1;
        try (Connection con = dataBaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(DBConstants.GET_NEXT_PARKING_SPOT)) {
            ps.setString(1, parkingType.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result = rs.getInt(1);
                }
            }
        } catch (Exception ex) {
            logger.error("Error fetching next available slot", ex);
        }
        return result;
    }

    public ParkingSpot getNextParkingSpot(ParkingType parkingType) {
        int nextAvailableSlot = getNextAvailableSlot(parkingType);
        if (nextAvailableSlot > 0) {
            return new ParkingSpot(nextAvailableSlot, parkingType, true);
        }
        return null;
    }

    public boolean updateParking(ParkingSpot parkingSpot) {
        // update the availability fo that parking slot
        try (Connection con = dataBaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_PARKING_SPOT)) {
            ps.setBoolean(1, parkingSpot.isAvailable());
            ps.setInt(2, parkingSpot.getId());
            int updateRowCount = ps.executeUpdate();
            return (updateRowCount == 1);
        } catch (Exception ex) {
            logger.error("Error updating parking info", ex);
            return false;
        }
    }

    public ParkingSpot getParkingSpot(int parkingSpotId) throws ClassNotFoundException {
        ParkingSpot parkingSpot = null;
        String query = "SELECT parking_id, parking_type, available FROM parking WHERE parking_id = ?";
        try (Connection connection = dataBaseConfig.getConnection();
                PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, parkingSpotId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ParkingType parkingType = ParkingType.valueOf(rs.getString("parking_type"));
                    boolean isAvailable = rs.getBoolean("available");
                    parkingSpot = new ParkingSpot(parkingSpotId, parkingType, isAvailable);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching parking spot", e);
        }
        return parkingSpot;
    }
}
