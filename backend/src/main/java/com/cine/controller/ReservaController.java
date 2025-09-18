package com.cine.controller;

import com.cine.domain.AsientoFuncion;
import com.cine.domain.Pago;
import com.cine.domain.Reserva;
import com.cine.domain.Usuario;
import com.cine.dto.HoldRequest;
import com.cine.repo.AsientoFuncionRepository;
import com.cine.repo.ReservaRepository;
import com.cine.repo.UsuarioRepository;
import com.cine.service.HoldService;
import com.cine.service.PagoService;
import com.cine.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReservaController {
    private static final Logger log = LoggerFactory.getLogger(ReservaController.class);
    private final HoldService holdService;
    private final ReservaService reservaService;
    private final PagoService pagoService;
    private final UsuarioRepository usuarioRepository;
    private final AsientoFuncionRepository asientoFuncionRepository;

    public ReservaController(HoldService holdService, ReservaService reservaService, PagoService pagoService,
                             UsuarioRepository usuarioRepository, AsientoFuncionRepository asientoFuncionRepository) {
        this.holdService = holdService;
        this.reservaService = reservaService;
        this.pagoService = pagoService;
        this.usuarioRepository = usuarioRepository;
        this.asientoFuncionRepository = asientoFuncionRepository;
    }

    @PostMapping(value = "/reservas/hold", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> hold(@Valid @RequestBody HoldRequest req, Authentication auth) {
    log.info("hold() called - auth param: {}", auth);
    log.info("hold() called - SecurityContext auth: {}", SecurityContextHolder.getContext().getAuthentication());
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        }
        String email = auth.getName();
        try {
            log.info("Attempting to hold seats for funcionId={} asientoIds={} by {}", req.getFuncionId(), req.getAsientoIds(), email);
            List<AsientoFuncion> held = holdService.holdSeats(req.getFuncionId(), req.getAsientoIds(), email, 10);
            log.info("Held seats: {}", held.stream().map(AsientoFuncion::getId).toList());
            List<Long> asientoFuncionIds = held.stream().map(AsientoFuncion::getId).toList();
            log.info("Looking up usuario by email {}", email);
            Usuario user = usuarioRepository.findByEmail(email).orElseThrow();
            log.info("Creating reserva for usuarioId={} funcionId={} seats={}", user.getId(), req.getFuncionId(), asientoFuncionIds);
            Reserva r = reservaService.crearReserva(user.getId(), req.getFuncionId(), asientoFuncionIds);
            log.info("Reserva created: id={} total={}", r.getId(), r.getTotal());
            return ResponseEntity.ok(Map.of("reservaId", r.getId(), "total", r.getTotal()));
        } catch (DataAccessException ex) {
            log.error("Data access error while holding seats (likely Redis): {}", ex.getMessage(), ex);
            return ResponseEntity.status(503).body(Map.of("error", "Unable to connect to Redis", "details", ex.getMessage()));
        } catch (Exception ex) {
            log.error("Error processing hold request: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
        }
    }

    public static record CheckoutReq(Long reservaId) {}

    @PostMapping("/reservas/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutReq req, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        }
        Pago p = pagoService.crearPagoCheckout(req.reservaId());
        var reserva = p.getReserva();
        long cents = reserva.getTotal().movePointRight(2).longValueExact();
        String signature = pagoService.buildIntegritySignature(p.getReferencia(), cents, pagoService.getCurrency());
        return ResponseEntity.ok(Map.of(
                "publicKey", pagoService.getWompiPublicKey(),
                "currency", pagoService.getCurrency(),
                "amountInCents", cents,
                "reference", p.getReferencia(),
                "redirectUrl", pagoService.getRedirectUrl(),
                "signature", signature
        ));
    }
}


