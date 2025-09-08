package com.cine.repo;

import com.cine.domain.AsientoFuncion;
import com.cine.domain.Funcion;
import com.cine.domain.Enums.EstadoAsientoFuncion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface AsientoFuncionRepository extends JpaRepository<AsientoFuncion, Long> {
    List<AsientoFuncion> findByFuncion(Funcion funcion);
    List<AsientoFuncion> findByFuncionAndEstado(Funcion funcion, EstadoAsientoFuncion estado);
    List<AsientoFuncion> findByEstadoAndHoldExpiresAtBefore(EstadoAsientoFuncion estado, OffsetDateTime before);
    Optional<AsientoFuncion> findByFuncionIdAndAsientoId(Long funcionId, Long asientoId);
}

