package com.gabro.kaffe.repository;

import com.gabro.kaffe.entity.Ruolo;
import com.gabro.kaffe.entity.Utente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface UtenteRepository extends JpaRepository<Utente, Long> {
    Optional<Utente> findByEmail(String email);
    List<Utente> findByRuolo(Ruolo ruolo);
    boolean existsByEmail(String email);
}