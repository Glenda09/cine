package com.cine.repo;

import com.cine.domain.Asiento;
import com.cine.domain.Sala;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AsientoRepository extends JpaRepository<Asiento, Long> {
    List<Asiento> findBySalaAndActivoTrue(Sala sala);
}

