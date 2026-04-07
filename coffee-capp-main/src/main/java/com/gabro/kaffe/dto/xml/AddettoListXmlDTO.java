package com.gabro.kaffe.dto.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "addetti-list")
public class AddettoListXmlDTO {

    @JacksonXmlProperty(localName = "addetto")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<AddettoXmlDTO> addetti;
}