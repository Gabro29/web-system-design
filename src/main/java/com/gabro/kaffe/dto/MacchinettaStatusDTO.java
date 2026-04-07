package com.gabro.kaffe.dto;

import java.util.List;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "macchinettaStatus")
public class MacchinettaStatusDTO {
    private String code;
    private boolean active;

    private String message;

    @JacksonXmlProperty(localName = "prodotti")
    private List<ProdottoDTO> prodotti;
}