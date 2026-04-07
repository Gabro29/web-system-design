package com.gabro.kaffe.service;

import com.gabro.kaffe.dto.ClienteDTO;

public interface ClienteService {
    ClienteDTO register(String email, String password);
    ClienteDTO getStato(String email);
    ClienteDTO connettiMacchinetta(String email, String machineCode);
    ClienteDTO disconnettiMacchinetta(String email);
    ClienteDTO ricarica(String email, Double importo);
}