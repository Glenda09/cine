package com.cine.controller;

import com.cine.domain.Usuario;
import com.cine.dto.AuthRegisterReq;
import com.cine.security.JwtUtil;
import com.cine.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.cine.domain.Enums.RolUsuario;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRegisterReq req) {
        Usuario u = authService.register(req.getNombre(), req.getEmail(), req.getPassword(), RolUsuario.USER);
        String token = jwtUtil.generateToken(u.getEmail(), Map.of("role", u.getRol().name(), "name", u.getNombre()));
        return ResponseEntity.ok(Map.of("token", token));
    }

    public static record LoginReq(String email, String password) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginReq req) {
        String token = authService.login(req.email(), req.password());
        return ResponseEntity.ok(Map.of("token", token));
    }

    // Ayuda: si acceden por GET a /api/auth/login o /api/auth/register, redirige a las páginas HTML
    @GetMapping("/login")
    public ResponseEntity<?> loginGet() {
        return ResponseEntity.status(302).header("Location", "/login").build();
    }
    @GetMapping("/register")
    public ResponseEntity<?> registerGet() {
        return ResponseEntity.status(302).header("Location", "/register").build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(org.springframework.security.core.Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(Map.of("principal", auth.getName(), "authorities", auth.getAuthorities()));
    }
}
