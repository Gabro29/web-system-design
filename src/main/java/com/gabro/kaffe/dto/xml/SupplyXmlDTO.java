package com.gabro.kaffe.dto.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class SupplyXmlDTO {

    @JacksonXmlProperty(localName = "glc:name")
    private String name;

    @JacksonXmlProperty(localName = "glc:level-percent")
    private int levelPercent;

    @JacksonXmlProperty(localName = "glc:capacity")
    private int capacity;

    @JacksonXmlProperty(localName = "glc:unit")
    private String unit;

    @JacksonXmlProperty(localName = "glc:warning-threshold")
    private int warningThreshold;


    public SupplyXmlDTO(String name, int livello, int capacita, String unita, int soglia) {
        this.name = name;
        this.capacity = capacita;
        this.unit = unita;
        this.levelPercent = (capacita > 0) ? (int) ((livello * 100.0) / capacita) : 0;
        this.warningThreshold = (capacita > 0) ? (int) ((soglia * 100.0) / capacita) : 0;
    }
}