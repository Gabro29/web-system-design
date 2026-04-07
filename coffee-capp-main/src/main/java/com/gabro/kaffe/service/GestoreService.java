package com.gabro.kaffe.service;

import com.gabro.kaffe.dto.GestoreDTO;
import com.gabro.kaffe.dto.xml.AddettoListXmlDTO;
import com.gabro.kaffe.dto.xml.MachineListXmlDTO;
import com.gabro.kaffe.entity.StatoMacchinetta;


public interface GestoreService {

    GestoreDTO getStato(String email);

    MachineListXmlDTO buildMachineStatusXml();
    AddettoListXmlDTO buildAddettiXml();

    void setMachineStatus(String machineId, StatoMacchinetta status);

    void removeMachine(String machineId);
    void addMachine(String code, String place);

    void removeAddetto(String email);
    void addAddetto(String email, String password);
}
