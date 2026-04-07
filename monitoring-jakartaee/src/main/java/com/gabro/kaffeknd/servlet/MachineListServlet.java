package com.gabro.kaffeknd.servlet;

import com.gabro.kaffeknd.dao.MonitoringDAO;
import com.gabro.kaffeknd.dto.MachineStatusDTO;
import com.gabro.kaffeknd.util.XmlResponseUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


@WebServlet(name = "MachineListServlet", value = "/monitoring/list")
public class MachineListServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(MachineListServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        MonitoringDAO dao = new MonitoringDAO();

        try {
            List<MachineStatusDTO> machineList = dao.getUpdatedMachineList();
            String xmlContent = XmlResponseUtil.buildMachineListXml(machineList);
            response.setContentType("text/xml;charset=UTF-8");
            response.getWriter().write(xmlContent);

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Errore recupero lista monitoraggio", e);
            XmlResponseUtil.sendGenericResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "ERROR", "DB_ERROR", "Errore Database Interno");
        }
    }
}