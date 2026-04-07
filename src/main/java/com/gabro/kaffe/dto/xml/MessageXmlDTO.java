package com.gabro.kaffe.dto.xml;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.gabro.kaffe.entity.TipoMessaggio;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageXmlDTO {

    @JacksonXmlProperty(localName = "glc:type")
    private TipoMessaggio type;

    @JacksonXmlProperty(localName = "glc:text")
    private String text;

    @JacksonXmlProperty(localName = "glc:timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}