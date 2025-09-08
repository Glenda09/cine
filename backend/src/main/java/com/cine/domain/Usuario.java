package com.cine.domain;

import jakarta.persistence.*;
import lombok.*;

import static com.cine.domain.Enums.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String hashPassword;
    @Enumerated(EnumType.STRING)
    private RolUsuario rol = RolUsuario.USER;
}

