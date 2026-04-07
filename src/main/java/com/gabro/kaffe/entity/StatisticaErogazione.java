package com.gabro.kaffe.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = "uq_statistica", columnNames = {"macchinetta_code", "prodotto_id"})})
public class StatisticaErogazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "macchinetta_code")
    private Macchinetta macchinetta;

    @ManyToOne
    @JoinColumn(name = "prodotto_id")
    private Prodotto prodotto;

    private int conteggio;


    public StatisticaErogazione (Macchinetta macchinetta, Prodotto prodotto, int conteggio) {
        this.macchinetta = macchinetta;
        this.prodotto = prodotto;
        this.conteggio = conteggio;
    }
}