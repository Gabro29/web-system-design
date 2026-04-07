package com.gabro.kaffeknd.servlet;

import com.gabro.kaffeknd.dao.SyncDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


@WebServlet(name = "SyncServlet", value = "/sync")
public class SyncServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(SyncServlet.class.getName());


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");
        String code = request.getParameter("code");

        if (code == null || code.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Code missing");
            return;
        }

        SyncDAO dao = new SyncDAO();
        try {
            if ("DELETE".equalsIgnoreCase(action)) {
                dao.deleteMachine(code);
                log.info("Macchina eliminata dal monitoraggio: " + code);
            } else {
                String status = request.getParameter("status");
                String latStr = request.getParameter("lat");
                String lngStr = request.getParameter("lng");

                double lat = (latStr != null) ? Double.parseDouble(latStr) : 38.10400;
                double lng = (lngStr != null) ? Double.parseDouble(lngStr) : 13.34800;
                if (status == null) status = "MAINTENANCE";

                dao.syncMachine(code, status, lat, lng);
                log.info("Macchina sincronizzata: " + code);
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("SYNC_OK");

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Errore DB Sync", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database Error");
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Lat/Lng format");
        }
    }
}