package com.cine.repo;

import com.cine.domain.Funcion;
import com.cine.domain.Pelicula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FuncionRepository extends JpaRepository<Funcion, Long> {
    List<Funcion> findByPeliculaAndHoraInicioBetween(Pelicula pelicula, LocalDateTime start, LocalDateTime end);
    List<Funcion> findByHoraInicioBetween(LocalDateTime start, LocalDateTime end);
}

