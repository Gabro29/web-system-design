package com.gabro.kaffe.entity;

import java.time.LocalDateTime;
import java.util.List;

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
public class Macchinetta {

    @Id
    @EqualsAndHashCode.Include
    private String code;

    @Enumerated(EnumType.STRING)
    private StatoMacchinetta status;

    private String luogo;

    @OneToOne
    @JoinColumn(name = "current_user_id")
    private Utente connectedUser;

    private LocalDateTime lastUserInteraction;

    @OneToMany(mappedBy = "macchinetta", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Fornitura> forniture;

    @OneToMany(mappedBy = "macchinetta", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Messaggio> messaggi;

    @OneToMany(mappedBy = "macchinetta", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<StatisticaErogazione> statistiche;
}