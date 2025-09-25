package com.cine.repo;

import com.cine.domain.Enums.EstadoReserva;
import com.cine.domain.Reserva;
import com.cine.domain.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    @EntityGraph(attributePaths = {
            "funcion",
            "funcion.pelicula",
            "funcion.sala",
            "asientos",
            "asientos.asiento"
    })
    List<Reserva> findByUsuarioOrderByCreatedAtDesc(Usuario usuario);

    List<Reserva> findByEstado(EstadoReserva estado);
}
