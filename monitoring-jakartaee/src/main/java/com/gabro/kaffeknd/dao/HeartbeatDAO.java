package com.gabro.kaffeknd.dao;

import com.gabro.kaffeknd.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class HeartbeatDAO {

    private static final String UPDATE_HEARTBEAT_SQL =
            "UPDATE machine_status SET last_heartbeat = NOW(), status = ? WHERE macchinetta_code = ?";


    public boolean updateHeartbeat(String code, String currentStatus) throws SQLException {

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_HEARTBEAT_SQL)) {
            ps.setString(1, currentStatus);
            ps.setString(2, code);
            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;
        }
    }
}