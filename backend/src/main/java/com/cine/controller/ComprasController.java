package com.cine.controller;

import com.cine.domain.AsientoFuncion;
import com.cine.domain.Enums.EstadoReserva;
import com.cine.domain.Reserva;
import com.cine.domain.Usuario;
import com.cine.repo.ReservaRepository;
import com.cine.repo.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

@Controller
public class ComprasController {
    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;

    public ComprasController(ReservaRepository reservaRepository, UsuarioRepository usuarioRepository) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/mis-compras")
    public String misCompras(Authentication auth, Model model) {
        if (auth == null) {
            populateEmptyModel(model);
            return "compras";
        }
        Usuario u = usuarioRepository.findByEmail(auth.getName()).orElse(null);
        if (u == null) {
            populateEmptyModel(model);
            return "compras";
        }
        List<CompraView> compras = reservaRepository.findByUsuarioOrderByCreatedAtDesc(u)
                .stream()
                .map(this::toView)
                .toList();

        model.addAttribute("recent", compras.stream().limit(3).toList());
        model.addAttribute("previous", compras.size() > 3 ? compras.subList(3, compras.size()) : List.of());
        model.addAttribute("hasPurchases", !compras.isEmpty());
        model.addAttribute("reservas", compras);
        return "compras";
    }

    private CompraView toView(Reserva reserva) {
        List<String> seats = reserva.getAsientos().stream()
                .sorted(Comparator.comparing((AsientoFuncion af) -> af.getAsiento().getFila())
                        .thenComparing(af -> af.getAsiento().getColumna()))
                .map(this::formatSeat)
                .toList();
        String seatsText = seats.isEmpty() ? "Sin asientos" : String.join(", ", seats);
        OffsetDateTime fecha = reserva.getCreatedAt();
        BigDecimal total = reserva.getTotal() != null ? reserva.getTotal() : BigDecimal.ZERO;
        return new CompraView(
                reserva.getId(),
                reserva.getFuncion().getPelicula().getTitulo(),
                reserva.getFuncion().getPelicula().getPosterUrl(),
                reserva.getFuncion().getSala().getNombre(),
                fecha,
                total,
                reserva.getEstado().name(),
                reserva.getEstado() == EstadoReserva.PAGADA,
                seatsText
        );
    }

    private String formatSeat(AsientoFuncion asientoFuncion) {
        Integer fila = asientoFuncion.getAsiento().getFila();
        Integer columna = asientoFuncion.getAsiento().getColumna();
        String row = fila != null && fila > 0 ? rowToLetters(fila) : "";
        return row + (columna != null ? columna : "");
    }

    private String rowToLetters(int fila) {
        StringBuilder sb = new StringBuilder();
        int n = fila;
        while (n > 0) {
            n--;
            sb.insert(0, (char) ('A' + (n % 26)));
            n /= 26;
        }
        return sb.toString();
    }

    private void populateEmptyModel(Model model) {
        model.addAttribute("recent", List.of());
        model.addAttribute("previous", List.of());
        model.addAttribute("hasPurchases", false);
        model.addAttribute("reservas", List.of());
    }

    private record CompraView(
            Long id,
            String titulo,
            String posterUrl,
            String sala,
            OffsetDateTime fecha,
            BigDecimal total,
            String estado,
            boolean pagada,
            String asientos
    ) {}
}
