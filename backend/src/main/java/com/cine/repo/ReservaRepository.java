package com.cine.repo;

import com.cine.domain.Reserva;
import com.cine.domain.Usuario;
import com.cine.domain.Enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByUsuarioOrderByCreatedAtDesc(Usuario usuario);
    List<Reserva> findByEstado(EstadoReserva estado);
}

