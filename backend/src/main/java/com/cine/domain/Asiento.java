package com.cine.domain;

import jakarta.persistence.*;
import lombok.*;

import static com.cine.domain.Enums.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Asiento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Sala sala;

    @Column(nullable = false)
    private Integer fila;
    @Column(nullable = false)
    private Integer columna;

    @Enumerated(EnumType.STRING)
    private TipoAsiento tipo = TipoAsiento.NORMAL;
    private boolean activo = true;
}

