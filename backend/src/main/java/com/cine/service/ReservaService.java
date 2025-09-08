package com.cine.service;

import com.cine.domain.*;
import com.cine.repo.AsientoFuncionRepository;
import com.cine.repo.FuncionRepository;
import com.cine.repo.ReservaRepository;
import com.cine.repo.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static com.cine.domain.Enums.*;

@Service
public class ReservaService {
    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final FuncionRepository funcionRepository;
    private final AsientoFuncionRepository asientoFuncionRepository;

    public ReservaService(ReservaRepository reservaRepository, UsuarioRepository usuarioRepository,
                          FuncionRepository funcionRepository, AsientoFuncionRepository asientoFuncionRepository) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.funcionRepository = funcionRepository;
        this.asientoFuncionRepository = asientoFuncionRepository;
    }

    @Transactional
    public Reserva crearReserva(Long usuarioId, Long funcionId, List<Long> asientoFuncionIds) {
        Usuario user = usuarioRepository.findById(usuarioId).orElseThrow();
        Funcion funcion = funcionRepository.findById(funcionId).orElseThrow();
        var asientos = asientoFuncionRepository.findAllById(asientoFuncionIds);
        if (asientos.size() != asientoFuncionIds.size()) throw new RuntimeException("Asientos inválidos");
        asientos.forEach(af -> {
            if (af.getEstado() != EstadoAsientoFuncion.HOLD) throw new RuntimeException("Asiento no en hold");
            af.setEstado(EstadoAsientoFuncion.RESERVADO);
            af.setHoldExpiresAt(null);
            asientoFuncionRepository.save(af);
        });

        BigDecimal total = funcion.getPrecioBase().multiply(BigDecimal.valueOf(asientos.size()));
        Reserva r = Reserva.builder()
                .usuario(user)
                .funcion(funcion)
                .asientos(new java.util.HashSet<>(asientos))
                .estado(EstadoReserva.PENDIENTE)
                .total(total)
                .createdAt(OffsetDateTime.now())
                .build();
        return reservaRepository.save(r);
    }

    @Transactional
    public void marcarReservaPagada(Long reservaId) {
        Reserva r = reservaRepository.findById(reservaId).orElseThrow();
        r.setEstado(EstadoReserva.PAGADA);
        r.getAsientos().forEach(af -> af.setEstado(EstadoAsientoFuncion.COMPRADO));
        reservaRepository.save(r);
    }

    @Transactional
    public void cancelarReserva(Long reservaId) {
        Reserva r = reservaRepository.findById(reservaId).orElseThrow();
        r.setEstado(EstadoReserva.CANCELADA);
        r.getAsientos().forEach(af -> {
            af.setEstado(EstadoAsientoFuncion.LIBRE);
            af.setHoldExpiresAt(null);
        });
        reservaRepository.save(r);
    }
}

