package com.gabro.kaffe.service.impl;

import com.gabro.kaffe.dto.AddettoDTO;
import com.gabro.kaffe.dto.xml.MachineListXmlDTO;
import com.gabro.kaffe.entity.StatoMacchinetta;
import com.gabro.kaffe.entity.Utente;
import com.gabro.kaffe.exception.CustomLogicException;
import com.gabro.kaffe.exception.ResourceNotFoundException;
import com.gabro.kaffe.repository.UtenteRepository;
import com.gabro.kaffe.service.AddettoService;
import org.springframework.stereotype.Service;


@Service
public class AddettoServiceImpl implements AddettoService {

    private final UtenteRepository utenteRepository;
    private final MachineOperationalService machineOps;


    public AddettoServiceImpl(UtenteRepository utenteRepository, MachineOperationalService machineOps) {
        this.utenteRepository = utenteRepository;
        this.machineOps = machineOps;
    }


    @Override
    public AddettoDTO getStato(String email) {
        Utente u = utenteRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato: " + email));
        return mapToDTO(u);
    }


    @Override
    public void setStatus(String machineId, StatoMacchinetta nuovoStato) {
        if (nuovoStato != StatoMacchinetta.ACTIVE && nuovoStato != StatoMacchinetta.MAINTENANCE) {
            throw new CustomLogicException("Nuovo stato non valido. Puoi impostare solo ACTIVE o MAINTENANCE.");
        }
        machineOps.changeStatus(machineId, nuovoStato);
    }


    @Override
    public void restoreSupplies(String machineId) {
        machineOps.refillSupplies(machineId);
    }


    @Override
    public MachineListXmlDTO buildMachineStatusXml() {
        return machineOps.generateMachinesXml();
    }


    private AddettoDTO mapToDTO(Utente u) {
        String username = u.getEmail().split("@")[0];
        return new AddettoDTO(u.getEmail(), username);
    }
}