package com.cine.service;

import com.cine.domain.Pago;
import com.cine.domain.Reserva;
import com.cine.repo.PagoRepository;
import com.cine.repo.ReservaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;

import static com.cine.domain.Enums.EstadoPago;
import static com.cine.domain.Enums.ProveedorPago;

@Service
public class PagoService {
    private final PagoRepository pagoRepository;
    private final ReservaRepository reservaRepository;

    private final String wompiPublicKey;
    private final String wompiIntegrityKey;
    private final String redirectUrl;
    private final String currency;
    private final String checkoutBase; // << unificado con tu compose

    public PagoService(PagoRepository pagoRepository,
                        ReservaRepository reservaRepository,
                        @Value("${wompi.publicKey}") String wompiPublicKey,
                        @Value("${wompi.integrityKey}") String wompiIntegrityKey,
                        @Value("${wompi.redirectUrl}") String redirectUrl,
                        @Value("${wompi.currency}") String currency,
                        @Value("${wompi.checkoutBase}") String checkoutBase) {
        this.pagoRepository = pagoRepository;
        this.reservaRepository = reservaRepository;
        this.wompiPublicKey = wompiPublicKey;
        this.wompiIntegrityKey = wompiIntegrityKey;
        this.redirectUrl = redirectUrl;
        this.currency = currency;
        this.checkoutBase = checkoutBase;
    }

    /** Crea el registro Pago en estado PENDING con referencia única */
    @Transactional
    public Pago crearPagoCheckout(Long reservaId) {
        Reserva r = reservaRepository.findById(reservaId).orElseThrow();
        String referencia = "RES-" + reservaId + "-" + UUID.randomUUID();

        Pago p = Pago.builder()
                .reserva(r)
                .proveedor(ProveedorPago.WOMPI)
                .status(EstadoPago.PENDING)
                .referencia(referencia)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        return pagoRepository.save(p);
    }

    /** Por si quieres reutilizar o regenerar la referencia */
    @Transactional
    public String ensurePagoReferenceForReserva(Long reservaId) {
        return pagoRepository.findFirstByReservaIdOrderByIdDesc(reservaId)
                .map(Pago::getReferencia)
                .orElseGet(() -> crearPagoCheckout(reservaId).getReferencia());
    }

    public String getWompiPublicKey() { return wompiPublicKey; }
    public String getRedirectUrl()    { return redirectUrl; }
    public String getCurrency()       { return currency; }
    public String getCheckoutBase()   { return checkoutBase; }

    /** Firma de integridad (orden: reference + amount-in-cents + currency + integrityKey) */
    public String buildIntegritySignature(String reference, long amountInCents, String currency) {
        try {
            String text = reference + amountInCents + currency + wompiIntegrityKey;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error construyendo firma Wompi", e);
        }
    }
}