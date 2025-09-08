package com.cine.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

import static com.cine.domain.Enums.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Pago {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Reserva reserva;

    @Enumerated(EnumType.STRING)
    private ProveedorPago proveedor = ProveedorPago.WOMPI;

    @Enumerated(EnumType.STRING)
    private EstadoPago status = EstadoPago.CREATED;

    @Column(unique = true)
    private String referencia;

    private String rawPayload;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
