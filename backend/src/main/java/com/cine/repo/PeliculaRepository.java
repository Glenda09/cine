package com.cine.repo;

import com.cine.domain.Pelicula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PeliculaRepository extends JpaRepository<Pelicula, Long> {

    @Query("SELECT DISTINCT p FROM Pelicula p LEFT JOIN FETCH p.categorias")
    List<Pelicula> findAllWithCategorias();
}
