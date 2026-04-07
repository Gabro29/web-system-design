package com.gabro.kaffe.repository;

import com.gabro.kaffe.entity.Prodotto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface ProdottoRepository extends JpaRepository<Prodotto, Long> {
    List<Prodotto> findByAvailableTrue();
}