package com.cine.controller;

import com.cine.domain.Reserva;
import com.cine.repo.ReservaRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
public class TicketController {
    private final ReservaRepository reservaRepository;

    public TicketController(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    @GetMapping("/tickets/{id}.png")
    public ResponseEntity<byte[]> ticket(@PathVariable Long id) throws Exception {
        Reserva r = reservaRepository.findById(id).orElseThrow();
        String payload = ("TICKET|RESERVA=" + r.getId() + "|USUARIO=" + r.getUsuario().getEmail()).toUpperCase();
        BitMatrix matrix = new MultiFormatWriter().encode(new String(payload.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8),
                BarcodeFormat.QR_CODE, 300, 300, Map.of(EncodeHintType.MARGIN, 1));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .contentType(MediaType.IMAGE_PNG)
                .body(out.toByteArray());
    }
}

