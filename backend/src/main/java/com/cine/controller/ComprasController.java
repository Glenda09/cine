package com.cine.controller;

import com.cine.domain.Usuario;
import com.cine.repo.ReservaRepository;
import com.cine.repo.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
            model.addAttribute("reservas", java.util.List.of());
            return "compras";
        }
        Usuario u = usuarioRepository.findByEmail(auth.getName()).orElse(null);
        if (u == null) {
            model.addAttribute("reservas", java.util.List.of());
            return "compras";
        }
        model.addAttribute("reservas", reservaRepository.findByUsuarioOrderByCreatedAtDesc(u));
        return "compras";
    }
}

