package com.gabro.kaffe.entity;

import java.math.BigDecimal;
import java.util.List;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class Prodotto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String categoria;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prezzo;

    @Column(name = "image_path")
    private String imagePath;

    private boolean available = true;

    @OneToMany(mappedBy = "prodotto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ingrediente> ingredienti;
}