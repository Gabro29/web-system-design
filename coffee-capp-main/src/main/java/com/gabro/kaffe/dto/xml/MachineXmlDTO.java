package com.gabro.kaffe.dto.xml;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.gabro.kaffe.entity.StatoMacchinetta;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
public class MachineXmlDTO {

    @JacksonXmlProperty(localName = "glc:machine-id")
    private String machineId;

    @JacksonXmlProperty(localName = "glc:request-time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime requestTime;

    @JacksonXmlProperty(localName = "glc:machine-place")
    private String machinePlace;

    @JacksonXmlProperty(localName = "glc:machine-status")
    private StatoMacchinetta machineStatus;

    @JacksonXmlProperty(localName = "glc:actual-connected-user-id")
    private String connectedUser;

    @JacksonXmlProperty(localName = "glc:product-list")
    private ProductListXmlWrapper productList;

    @JacksonXmlProperty(localName = "glc:supply-list")
    private SupplyListXmlWrapper supplyList;

    @JacksonXmlProperty(localName = "glc:message-from-server-list")
    private MessageListXmlWrapper messageList;
}