package com.gabro.kaffe.service.impl;

import com.gabro.kaffe.dto.ClienteDTO;
import com.gabro.kaffe.entity.Macchinetta;
import com.gabro.kaffe.entity.Ruolo;
import com.gabro.kaffe.entity.StatoMacchinetta;
import com.gabro.kaffe.entity.Utente;
import com.gabro.kaffe.exception.CustomLogicException;
import com.gabro.kaffe.exception.ResourceNotFoundException;
import com.gabro.kaffe.repository.MacchinettaRepository;
import com.gabro.kaffe.repository.UtenteRepository;
import com.gabro.kaffe.service.ClienteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class ClienteServiceImpl implements ClienteService {


    private final UtenteRepository utenteRepository;
    private final MacchinettaRepository macchinettaRepository;
    private final PasswordEncoder passwordEncoder;


    public ClienteServiceImpl(UtenteRepository utenteRepository, MacchinettaRepository macchinettaRepository, PasswordEncoder passwordEncoder) {
        this.utenteRepository = utenteRepository;
        this.macchinettaRepository = macchinettaRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    @Transactional
    public ClienteDTO register(String email, String password) {
        String raffEmail = email.trim().toLowerCase();
        if (utenteRepository.findByEmail(raffEmail).isPresent()) {
            throw new CustomLogicException("Esiste già un utente con questa email: " + raffEmail);
        }
        Utente nuovoUtente = new Utente();
        nuovoUtente.setEmail(raffEmail);
        nuovoUtente.setPassword(passwordEncoder.encode(password));
        nuovoUtente.setCredito(BigDecimal.ZERO);
        nuovoUtente.setRuolo(Ruolo.CLIENTE);
        utenteRepository.save(nuovoUtente);
        return mapToDTO(nuovoUtente);
    }


    @Override
    public ClienteDTO getStato(String email) {
        Utente u = findUtenteByEmail(email);
        return mapToDTO(u);
    }


    @Override
    @Transactional
    public ClienteDTO connettiMacchinetta(String email, String machineCode) {
        Utente u = findUtenteByEmail(email);
        Macchinetta m = macchinettaRepository.findByCode(machineCode)
                .orElseThrow(() -> new ResourceNotFoundException("Macchinetta non esistente: " + machineCode));

        StatoMacchinetta status = m.getStatus();
        if (status != StatoMacchinetta.ACTIVE) {
            throw new CustomLogicException("Macchinetta non disponibile al momento (Stato: " + (status != null ? status : "N/A") + ")");
        }

        if (m.getConnectedUser() != null && !m.getConnectedUser().getId().equals(u.getId())) {
            throw new CustomLogicException("Macchinetta già occupata!");
        }

        m.setConnectedUser(u);
        m.setLastUserInteraction(LocalDateTime.now());
        macchinettaRepository.save(m);

        return mapToDTO(u);
    }


    @Override
    @Transactional
    public ClienteDTO disconnettiMacchinetta(String email) {
        Utente u = findUtenteByEmail(email);
        Optional<Macchinetta> mOpt = macchinettaRepository.findByConnectedUser(u);

        if (mOpt.isPresent()) {
            Macchinetta m = mOpt.get();
            m.setConnectedUser(null);
            m.setLastUserInteraction(null);
            macchinettaRepository.save(m);
        }
        return mapToDTO(u);
    }


    @Override
    @Transactional
    public ClienteDTO ricarica(String email, Double importo) {
        Utente u = findUtenteByEmail(email);
        u.setCredito(u.getCredito().add(BigDecimal.valueOf(importo)));
        utenteRepository.save(u);
        return mapToDTO(u);
    }


    private ClienteDTO mapToDTO(Utente u) {
        String machineId = null;
        Optional<Macchinetta> mOpt = macchinettaRepository.findByConnectedUser(u);
        if(mOpt.isPresent()) {
            machineId = mOpt.get().getCode();
        }
        String username = u.getEmail().split("@")[0];
        return new ClienteDTO(u.getEmail(), username, machineId, u.getCredito());
    }


    private Utente findUtenteByEmail(String email) {
        return utenteRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato: " + email));
    }
}