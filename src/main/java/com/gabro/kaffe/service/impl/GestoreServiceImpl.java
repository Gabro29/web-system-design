package com.gabro.kaffe.service.impl;

import com.gabro.kaffe.dto.GestoreDTO;
import com.gabro.kaffe.dto.xml.AddettoListXmlDTO;
import com.gabro.kaffe.dto.xml.MachineListXmlDTO;
import com.gabro.kaffe.entity.Macchinetta;
import com.gabro.kaffe.entity.StatoMacchinetta;
import com.gabro.kaffe.entity.Utente;
import com.gabro.kaffe.exception.CustomLogicException;
import com.gabro.kaffe.exception.ResourceNotFoundException;
import com.gabro.kaffe.repository.UtenteRepository;
import com.gabro.kaffe.service.GestoreService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class GestoreServiceImpl implements GestoreService {


    private final UtenteRepository utenteRepository;
    private final MachineOperationalService machineOps;
    private final PasswordEncoder passwordEncoder;


    public GestoreServiceImpl(UtenteRepository utenteRepository, MachineOperationalService machineOps, PasswordEncoder passwordEncoder) {
        this.utenteRepository = utenteRepository;
        this.machineOps = machineOps;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public GestoreDTO getStato(String email) {
        Utente u = utenteRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato: " + email));
        return mapToDTO(u);
    }


    @Override
    public MachineListXmlDTO buildMachineStatusXml() {
        return machineOps.generateMachinesXml();
    }


    @Override
    public AddettoListXmlDTO buildAddettiXml() {
        return machineOps.generateAddettiXml();
    }


    @Override
    public void setMachineStatus(String machineId, StatoMacchinetta nuovoStato) {
        Macchinetta m = machineOps.findByCode(machineId);
        StatoMacchinetta statoAttuale = m.getStatus();

        if (nuovoStato == StatoMacchinetta.ACTIVE) {
            if (statoAttuale != StatoMacchinetta.MAINTENANCE) {
                throw new CustomLogicException(
                        "Il distributore è in stato " + statoAttuale +
                                ". Solo le macchine MAINTENANCE possono essere attivate."
                );
            }
        }
        else if (nuovoStato == StatoMacchinetta.MAINTENANCE) {
            if (statoAttuale != StatoMacchinetta.ACTIVE) {
                throw new CustomLogicException(
                        "Il distributore è in stato " + statoAttuale +
                                ". Solo le macchine ACTIVE possono essere disattivate."
                );
            }
        }
        else {
            throw new CustomLogicException(
                    "Operazione non consentita: Il Gestore può solo Attivare o Disattivare i distributori."
            );
        }
        machineOps.changeStatus(machineId, nuovoStato);
    }


    @Override
    public void removeMachine(String machineId) {
        Macchinetta m = machineOps.findByCode(machineId);
        if (m.getStatus() != StatoMacchinetta.MAINTENANCE) {
            throw new CustomLogicException(
                    "Impossibile rimuovere il distributore " + machineId +
                            ". Per motivi di sicurezza, devi prima disattivarlo (Stato richiesto: MAINTENANCE)."
            );
        }
        machineOps.deleteMachine(machineId);
    }


    @Override
    public void addMachine(String code, String place) {
        if (code == null || !code.matches("^[A-Z]{2}[0-9]{3}[A-Z]{2}$")) {
            throw new CustomLogicException("Formato ID non valido. Usa: AA123BB");
        }

        if (place == null || place.trim().isEmpty()) {
            throw new CustomLogicException("Il luogo non può essere vuoto");
        }

        machineOps.createMachine(code, place);
    }


    @Override
    public void removeAddetto(String email) {
        machineOps.deleteAddetto(email);
    }


    @Override
    public void addAddetto(String email, String password) {
        if (email == null || !email.contains("@")) {
            throw new CustomLogicException("Formato email non valido");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new CustomLogicException("La password non può essere vuota");
        }
        machineOps.createAddetto(email, passwordEncoder.encode(password));
    }


    private GestoreDTO mapToDTO(Utente u) {
        String username = u.getEmail().split("@")[0];
        return new GestoreDTO(u.getEmail(), username);
    }
}