package com.parkit.parkingsystem.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class DataBaseConfig {


    public Connection getConnection() throws ClassNotFoundException, SQLException {
        System.out.println("Create DB connection");
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/prod", System.getenv("DB_USER"), System.getenv("DB_PASSWORD"));
    }

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
