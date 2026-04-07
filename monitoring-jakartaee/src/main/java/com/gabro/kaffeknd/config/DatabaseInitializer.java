package com.gabro.kaffeknd.config;

import com.gabro.kaffeknd.util.DBConnection;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;


@WebListener
public class DatabaseInitializer implements ServletContextListener {

    private static final Logger log = Logger.getLogger(DatabaseInitializer.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("--- START INIT: Database Jakarta ---");

        String[] sqlStatements = {
                "CREATE TABLE IF NOT EXISTS machine_status (" +
                        "   macchinetta_code VARCHAR(50) PRIMARY KEY," +
                        "   status VARCHAR(20) NOT NULL," +
                        "   last_heartbeat TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "   latitude DOUBLE," +
                        "   longitude DOUBLE" +
                        ")",

                "INSERT IGNORE INTO machine_status (macchinetta_code, status, latitude, longitude) VALUES " +
                        "('FE716XW', 'ACTIVE',      38.10543, 13.35086), " +
                        "('DR529QP', 'MAINTENANCE', 38.10488, 13.34960), " +
                        "('AB123CD', 'OFFLINE',     38.10705, 13.35031), " +
                        "('GH890LM', 'ERROR',       38.10419, 13.34863)"
        };

        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement()) {

            for (String sql : sqlStatements) {
                stmt.executeUpdate(sql);
            }

            log.info("--- SUCCESS: Tabelle ricreate e dati di test inseriti ---");

        } catch (SQLException e) {
            log.severe("!!! ERRORE CRITICO INIT DB: " + e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Monitoraggio Shutdown.");
    }
}