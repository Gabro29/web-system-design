package com.gabro.kaffeknd.util;

import com.gabro.kaffeknd.dto.MachineStatusDTO;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


public class XmlResponseUtil {

    public static String buildMachineListXml(List<MachineStatusDTO> machines) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<monitoring-list>\n");

        for (MachineStatusDTO m : machines) {
            sb.append("<machine>\n");
            sb.append("<code>").append(m.getCode()).append("</code>\n");
            sb.append("<status>").append(m.getStatus()).append("</status>\n");
            sb.append("<lat>").append(m.getLat()).append("</lat>\n");
            sb.append("<lng>").append(m.getLng()).append("</lng>\n");
            sb.append("</machine>\n");
        }
        sb.append("</monitoring-list>");
        return sb.toString();
    }


    public static void sendGenericResponse(HttpServletResponse response, int httpStatus,
                                           String status, String errorCode, String message) throws IOException {

        response.setStatus(httpStatus);
        response.setContentType("text/xml;charset=UTF-8");

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<response>\n");
        sb.append("<status>").append(status).append("</status>\n");

        if (errorCode != null) {
            sb.append("<errorCode>").append(errorCode).append("</errorCode>\n");
        }

        sb.append("<message>").append(message).append("</message>\n");
        sb.append("</response>");

        PrintWriter out = response.getWriter();
        out.print(sb);
        out.flush();
    }
}