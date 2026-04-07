package com.gabro.kaffeknd.dao;

import com.gabro.kaffeknd.dto.MachineStatusDTO;
import com.gabro.kaffeknd.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class MonitoringDAO {


    private static final String UPDATE_OFFLINE_SQL =
            "UPDATE machine_status SET status = 'OFFLINE' " +
                    "WHERE last_heartbeat < (NOW() - INTERVAL 3 MINUTE)";


    private static final String SELECT_ALL_SQL =
            "SELECT macchinetta_code, status, latitude, longitude FROM machine_status";


    public List<MachineStatusDTO> getUpdatedMachineList() throws SQLException {
        List<MachineStatusDTO> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement psUpdate = con.prepareStatement(UPDATE_OFFLINE_SQL)) {
                psUpdate.executeUpdate();
            }
            try (PreparedStatement psSelect = con.prepareStatement(SELECT_ALL_SQL);
                 ResultSet rs = psSelect.executeQuery()) {
                while (rs.next()) {
                    list.add(new MachineStatusDTO(
                            rs.getString("macchinetta_code"),
                            rs.getString("status"),
                            rs.getDouble("latitude"),
                            rs.getDouble("longitude")
                    ));
                }
            }
        }
        return list;
    }
}