package com.gabro.kaffe.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JacksonXmlRootElement(localName = "prodotto")
public class ProdottoDTO {
    private Long id;
    private String nome;
    private BigDecimal prezzo;
    private String imagePath;
}