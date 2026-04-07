package com.gabro.kaffe.dto.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductXmlDTO {

    @JacksonXmlProperty(localName = "glc:category")
    private String category;

    @JacksonXmlProperty(localName = "glc:name")
    private String name;

    @JacksonXmlProperty(localName = "glc:price")
    private BigDecimal price;

    @JacksonXmlProperty(localName = "glc:available")
    private boolean available;

    @JacksonXmlProperty(localName = "glc:times-served")
    private int timesServed;
}