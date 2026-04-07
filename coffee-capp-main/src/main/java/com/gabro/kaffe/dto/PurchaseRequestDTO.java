package com.gabro.kaffe.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "purchaseRequest")
public class PurchaseRequestDTO {
    private String machineCode;
    private Long productId;
    private int sugarLevel;
}