package com.gabro.kaffe.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.gabro.kaffe.entity.StatoMacchinetta;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "pollStatus")
public class MacchinettaPollDTO {
    private String machineCode;
    private boolean userConnected;

    @JacksonXmlProperty(localName = "username")
    private String userName;

    @JacksonXmlProperty(localName = "credit")
    private BigDecimal userCredit;

    @JacksonXmlProperty(localName = "status")
    private StatoMacchinetta status;
}