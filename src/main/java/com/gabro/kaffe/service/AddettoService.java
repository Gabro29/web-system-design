package com.gabro.kaffe.service;

import com.gabro.kaffe.dto.AddettoDTO;
import com.gabro.kaffe.dto.xml.MachineListXmlDTO;
import com.gabro.kaffe.entity.StatoMacchinetta;


public interface AddettoService {
    AddettoDTO getStato(String email);
    void setStatus(String machineId, StatoMacchinetta status);
    void restoreSupplies(String machineId);
    MachineListXmlDTO buildMachineStatusXml();
}