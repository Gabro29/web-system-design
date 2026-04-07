package com.gabro.kaffeknd.dao;

import com.gabro.kaffeknd.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SyncDAO {

    private static final String UPSERT_MACHINE_SQL =
            "INSERT INTO machine_status (macchinetta_code, status, latitude, longitude) " +
                    "VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE latitude = VALUES(latitude), longitude = VALUES(longitude), status = VALUES(status)";

    private static final String DELETE_MACHINE_SQL =
            "DELETE FROM machine_status WHERE macchinetta_code = ?";


    public void syncMachine(String code, String status, double lat, double lng) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(UPSERT_MACHINE_SQL)) {

            ps.setString(1, code);
            ps.setString(2, status);
            ps.setDouble(3, lat);
            ps.setDouble(4, lng);

            ps.executeUpdate();
        }
    }


    public void deleteMachine(String code) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_MACHINE_SQL)) {
            ps.setString(1, code);
            ps.executeUpdate();
        }
    }
}