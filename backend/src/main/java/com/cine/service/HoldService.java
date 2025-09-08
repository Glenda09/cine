package com.cine.service;

import com.cine.domain.AsientoFuncion;
import com.cine.domain.Enums.EstadoAsientoFuncion;
import com.cine.domain.Funcion;
import com.cine.repo.AsientoFuncionRepository;
import com.cine.repo.FuncionRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HoldService {
    private final StringRedisTemplate redis;
    private final AsientoFuncionRepository asientoFuncionRepository;
    private final FuncionRepository funcionRepository;

    public HoldService(StringRedisTemplate redis, AsientoFuncionRepository asientoFuncionRepository, FuncionRepository funcionRepository) {
        this.redis = redis;
        this.asientoFuncionRepository = asientoFuncionRepository;
        this.funcionRepository = funcionRepository;
    }

    private String key(Long funcionId, Long asientoId) {
        return "hold:%d:%d".formatted(funcionId, asientoId);
    }

    @Transactional
    public List<AsientoFuncion> holdSeats(Long funcionId, List<Long> asientoIds, String holdOwner, int ttlMinutes) {
        Funcion funcion = funcionRepository.findById(funcionId).orElseThrow();
        OffsetDateTime expires = OffsetDateTime.now().plusMinutes(ttlMinutes);

        // Check and set holds in Redis atomically per seat
        var acquired = asientoIds.stream().allMatch(seatId ->
                Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(key(funcionId, seatId), holdOwner, Duration.ofMinutes(ttlMinutes)))
        );
        if (!acquired) {
            // Rollback any partial holds
            asientoIds.forEach(seatId -> redis.delete(key(funcionId, seatId)));
            throw new RuntimeException("Algún asiento ya está en hold o reservado");
        }

        // Mark in DB
        List<AsientoFuncion> updated = asientoIds.stream().map(seatId -> {
            AsientoFuncion af = asientoFuncionRepository.findByFuncionIdAndAsientoId(funcionId, seatId).orElseThrow();
            if (af.getEstado() != EstadoAsientoFuncion.LIBRE) throw new RuntimeException("Asiento no disponible");
            af.setEstado(EstadoAsientoFuncion.HOLD);
            af.setHoldExpiresAt(expires);
            return af;
        }).collect(Collectors.toList());
        asientoFuncionRepository.saveAll(updated);
        return updated;
    }

    @Transactional
    public void releaseExpiredHolds() {
        var list = asientoFuncionRepository.findByEstadoAndHoldExpiresAtBefore(EstadoAsientoFuncion.HOLD, OffsetDateTime.now());
        for (var af : list) {
            af.setEstado(EstadoAsientoFuncion.LIBRE);
            af.setHoldExpiresAt(null);
            asientoFuncionRepository.save(af);
            redis.delete(key(af.getFuncion().getId(), af.getAsiento().getId()));
        }
    }
}

