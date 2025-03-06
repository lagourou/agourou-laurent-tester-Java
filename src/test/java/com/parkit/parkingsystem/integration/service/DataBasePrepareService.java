package com.parkit.parkingsystem.integration.service;

import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;

import java.sql.Connection;
import java.sql.SQLException;

public class DataBasePrepareService {

    DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();

    public void clearDataBaseEntries() {
        Connection connection = null;
        try {
            connection = dataBaseTestConfig.getConnection();

            // set parking entries to available
            connection.prepareStatement("update parking set available = true").execute();

            connection.prepareStatement("truncate table ticket").execute();

        } catch (ClassNotFoundException | SQLException e) {
        } finally {
            dataBaseTestConfig.closeConnection(connection);
        }
    }

}
