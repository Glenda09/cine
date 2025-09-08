package com.cine.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.cine.domain.Enums.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Funcion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Pelicula pelicula;

    @ManyToOne(optional = false)
    private Sala sala;

    @Column(nullable = false)
    private LocalDateTime horaInicio;

    @Enumerated(EnumType.STRING)
    private Idioma idioma = Idioma.SUB;

    @Enumerated(EnumType.STRING)
    private Formato formato = Formato.DOSD;

    @Column(nullable = false)
    private BigDecimal precioBase;
}

