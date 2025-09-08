package com.cine.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

import static com.cine.domain.Enums.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AsientoFuncion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Funcion funcion;

    @ManyToOne(optional = false)
    private Asiento asiento;

    @Enumerated(EnumType.STRING)
    private EstadoAsientoFuncion estado = EstadoAsientoFuncion.LIBRE;

    private OffsetDateTime holdExpiresAt;
}

