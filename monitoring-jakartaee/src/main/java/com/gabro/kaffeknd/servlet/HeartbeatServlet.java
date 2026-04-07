package com.gabro.kaffeknd.servlet;

import com.gabro.kaffeknd.dao.HeartbeatDAO;
import com.gabro.kaffeknd.util.XmlResponseUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


@WebServlet(name = "heartBeatServlet", value = "/heartbeat")
public class HeartbeatServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(HeartbeatServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String macchinettaCode = request.getParameter("macchinetta_code");
        String machineStatus = request.getParameter("status");

        if (macchinettaCode == null || macchinettaCode.trim().isEmpty() || machineStatus == null || machineStatus.trim().isEmpty()) {

            XmlResponseUtil.sendGenericResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                    "ERROR", "MISSING_PARAM", "Codice o Stato mancante");
            return;
        }

        HeartbeatDAO dao = new HeartbeatDAO();

        try {
            boolean success = dao.updateHeartbeat(macchinettaCode, machineStatus);

            if (success) {
                log.info("Heartbeat ricevuto: " + macchinettaCode);
                XmlResponseUtil.sendGenericResponse(response, HttpServletResponse.SC_OK,
                        "OK", null, "Stato aggiornato");
            } else {
                log.warning("Macchina sconosciuta: " + macchinettaCode);
                XmlResponseUtil.sendGenericResponse(response, HttpServletResponse.SC_NOT_FOUND,
                        "ERROR", "UNKNOWN_MACHINE", "Macchina non registrata");
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Errore SQL", e);
            XmlResponseUtil.sendGenericResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "ERROR", "DB_ERROR", "Errore Database");
        }
    }
}