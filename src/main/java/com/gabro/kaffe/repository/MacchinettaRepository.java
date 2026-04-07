package com.gabro.kaffe.repository;

import com.gabro.kaffe.entity.Macchinetta;
import com.gabro.kaffe.entity.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface MacchinettaRepository extends JpaRepository<Macchinetta, String> {
    Optional<Macchinetta> findByCode(String code);
    Optional<Macchinetta> findByConnectedUser(Utente connectedUser);
}