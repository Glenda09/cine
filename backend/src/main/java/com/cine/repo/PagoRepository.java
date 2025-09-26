package com.cine.repo;

import com.cine.domain.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    // último pago de una reserva (por si ya existe uno)
    Optional<Pago> findFirstByReservaIdOrderByIdDesc(Long reservaId);

    // usado por el webhook para localizar el pago por referencia
    Optional<Pago> findByReferencia(String referencia);
}
