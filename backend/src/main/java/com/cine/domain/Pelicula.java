package com.cine.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

import static com.cine.domain.Enums.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Pelicula {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String titulo;
    @Column(length = 2000)
    private String sinopsis;
    private Integer duracionMin;
    private String posterUrl;
    private String trailerUrl;

    @ManyToMany
    @JoinTable(name = "pelicula_categoria",
            joinColumns = @JoinColumn(name = "pelicula_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id"))
    private Set<Categoria> categorias = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private TipoPelicula tipo;
    @Enumerated(EnumType.STRING)
    private ClasificacionEdad clasificacionEdad;
    @Enumerated(EnumType.STRING)
    private EstadoPelicula estado = EstadoPelicula.ACTIVA;
}

