package com.cine.controller;

import com.cine.domain.Pago;
import com.cine.repo.PagoRepository;
import com.cine.service.ReservaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static com.cine.domain.Enums.EstadoPago;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {
    private final String eventsSecret;
    private final PagoRepository pagoRepository;
    private final ReservaService reservaService;

    public WebhookController(@Value("${wompi.eventsSecret}") String eventsSecret,
                             PagoRepository pagoRepository, ReservaService reservaService) {
        this.eventsSecret = eventsSecret;
        this.pagoRepository = pagoRepository;
        this.reservaService = reservaService;
    }

    @PostMapping("/wompi")
    public ResponseEntity<?> wompi(HttpServletRequest request, @RequestHeader("X-Event-Signature") String signature) throws Exception {
        String raw = readBody(request);
        if (!verifySignature(raw, signature)) return ResponseEntity.status(400).body("Invalid signature");

        // parse minimal payload
        // Expect structure with transaction and reference
        String lower = raw.toLowerCase();
        boolean approved = lower.contains("\"status\":\"approved\"");
        int refIdx = lower.indexOf("\"reference\":");
        if (refIdx < 0) return ResponseEntity.ok().build();
        int start = lower.indexOf('"', refIdx + 12) + 1;
        int end = lower.indexOf('"', start);
        String reference = raw.substring(start, end);

        Pago pago = pagoRepository.findByReferencia(reference).orElse(null);
        if (pago != null) {
            if (approved) {
                pago.setStatus(EstadoPago.APPROVED);
                reservaService.marcarReservaPagada(pago.getReserva().getId());
            } else {
                pago.setStatus(EstadoPago.DECLINED);
            }
            pago.setRawPayload(raw);
            pagoRepository.save(pago);
        }
        return ResponseEntity.ok().build();
    }

    private String readBody(HttpServletRequest request) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    private boolean verifySignature(String rawBody, String header) throws Exception {
        // Header format: t=timestamp, v1=hexSig
        String[] parts = header.split(",");
        String v1 = null;
        for (String p : parts) {
            String[] kv = p.trim().split("=");
            if (kv.length == 2 && kv[0].equals("v1")) v1 = kv[1];
        }
        if (v1 == null) return false;
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        mac.init(new javax.crypto.spec.SecretKeySpec(eventsSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] sig = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
        String hex = HexFormat.of().formatHex(sig);
        return hex.equalsIgnoreCase(v1);
    }
}

