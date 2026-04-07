package com.gabro.kaffe.dto.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageListXmlWrapper {

    @JacksonXmlProperty(localName = "glc:message")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<MessageXmlDTO> messages;
}