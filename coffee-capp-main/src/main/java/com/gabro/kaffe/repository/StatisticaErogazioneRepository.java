package com.gabro.kaffe.repository;

import com.gabro.kaffe.entity.Macchinetta;
import com.gabro.kaffe.entity.Prodotto;
import com.gabro.kaffe.entity.StatisticaErogazione;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface StatisticaErogazioneRepository extends JpaRepository<StatisticaErogazione, Long> {
    Optional<StatisticaErogazione> findByMacchinettaAndProdotto(Macchinetta macchinetta, Prodotto prodotto);
    List<StatisticaErogazione> findByMacchinetta(Macchinetta m);
}