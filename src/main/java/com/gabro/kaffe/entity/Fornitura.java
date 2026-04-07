package com.gabro.kaffe.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = "uq_fornitura_macchina", columnNames = {"nome", "macchinetta_code"})})
public class Fornitura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private Integer livello;

    @Column(name = "capacita_massima")
    private Integer capacitaMassima;

    @Column(name = "soglia_attenzione")
    private Integer sogliaAttenzione;

    private String unita;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "macchinetta_code")
    @ToString.Exclude
    @JsonIgnore
    private Macchinetta macchinetta;
}