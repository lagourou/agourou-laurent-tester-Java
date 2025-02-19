package com.parkit.parkingsystem.integration.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.parkit.parkingsystem.config.DataBaseConfig;

public class DataBaseTestConfig extends DataBaseConfig {


    public void clearDataBase() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/parkingsystem", "root",
                "rootpassword");
                Statement stmt = con.createStatement()) {
            stmt.execute("TRUNCATE TABLE ticket");
            stmt.execute("TRUNCATE TABLE parking");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Connection getConnection() throws ClassNotFoundException, SQLException {
    System.out.println("Create DB connection");
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/test", "root", "rootroot");
    }

    @Override
    public void closeConnection(Connection con) {
        if (con != null) {
            try {
                con.close();
                System.out.println("Closing DB connection");
            } catch (SQLException e) {
            System.err.println("Error while closing connection");
            }
        }
    }

    @Override
    public void closePreparedStatement(PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
                System.out.println("Closing Prepared Statement");
            } catch (SQLException e) {
                System.err.println("Error while closing prepared statement");
            }
        }
    }

    @Override
    public void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
                System.out.println("Closing Result Set");
            } catch (SQLException e) {
                System.err.println("Error while closing result set");
            }
        }
    }
}
