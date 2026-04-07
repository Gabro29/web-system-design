package com.gabro.kaffeknd.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;


public class DBConnection {

    private static final String DATASOURCE_NAME = "java:comp/env/jdbc/kaffe";


    public static Connection getConnection() throws SQLException {
        try {
            Context initContext = new InitialContext();
            DataSource ds = (DataSource) initContext.lookup(DATASOURCE_NAME);
            return ds.getConnection();

        } catch (Exception e) {
            System.err.println("Errore connessione database:");
            System.err.println("JNDI Name: " + DATASOURCE_NAME);
            System.err.println("Errore: " + e.getMessage());
            throw new SQLException("Impossibile ottenere connessione al database", e);
        }
    }
}
