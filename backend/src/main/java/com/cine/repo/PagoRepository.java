package com.cine.repo;

import com.cine.domain.Pago;
import com.cine.domain.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    Optional<Pago> findByReferencia(String referencia);
    Optional<Pago> findByReserva(Reserva reserva);
}

