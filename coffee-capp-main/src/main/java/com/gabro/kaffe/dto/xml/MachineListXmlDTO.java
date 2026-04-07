package com.gabro.kaffe.dto.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@JacksonXmlRootElement(localName = "glc:machine-list")
public class MachineListXmlDTO {


    @JacksonXmlProperty(isAttribute = true, localName = "xmlns:glc")
    private String glcNamespace = "http://gabro.dev/capp";


    @JacksonXmlProperty(isAttribute = true, localName = "xmlns:xsi")
    private String xsiNamespace = "http://www.w3.org/2001/XMLSchema-instance";


    @JacksonXmlProperty(isAttribute = true, localName = "xsi:schemaLocation")
    private String schemaLocation = "http://gabro.dev/capp machines-schema.xsd";


    @JacksonXmlProperty(localName = "glc:machine")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<MachineXmlDTO> machines;

    public MachineListXmlDTO(List<MachineXmlDTO> machines) {
        this.machines = machines;
    }
}