package com.parkit.parkingsystem.constants;

public class DBConstants {

    // Private constructor to hide the implicit public one
    private DBConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String GET_PARKING_SPOT = "SELECT PARKING_NUMBER, TYPE, available FROM parking WHERE PARKING_NUMBER = ?";
    public static final String UPDATE_PARKING_SPOT = "UPDATE parking SET AVAILABLE = ? WHERE PARKING_NUMBER = ?";
    public static final String GET_NEXT_PARKING_SPOT = "SELECT PARKING_NUMBER FROM parking WHERE AVAILABLE = TRUE AND TYPE = ? ORDER BY PARKING_NUMBER LIMIT 1";

    public static final String SAVE_TICKET = "INSERT INTO ticket (PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME) VALUES (?, ?, ?, ?, ?)";
    public static final String UPDATE_TICKET = "UPDATE ticket SET PRICE=?, OUT_TIME=? WHERE ID=?";
    public static final String GET_TICKET = "SELECT t.ID, t.PARKING_NUMBER, t.VEHICLE_REG_NUMBER, t.PRICE, t.IN_TIME, t.OUT_TIME, p.TYPE FROM ticket t INNER JOIN parking p WHERE t.PARKING_NUMBER = p.PARKING_NUMBER AND t.VEHICLE_REG_NUMBER=? ORDER BY t.IN_TIME DESC LIMIT 1";
    public static final String COUNT_TICKET = "select count(*) from ticket where VEHICLE_REG_NUMBER = ?";
}
