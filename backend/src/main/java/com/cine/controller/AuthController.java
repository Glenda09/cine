package com.cine.controller;

import com.cine.domain.Usuario;
import com.cine.dto.AuthRegisterReq;
import com.cine.security.JwtUtil;
import com.cine.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.cine.domain.Enums.RolUsuario;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRegisterReq req) {
        Usuario u = authService.register(req.getNombre(), req.getEmail(), req.getPassword(), RolUsuario.USER);
        String token = jwtUtil.generateToken(u.getEmail(), Map.of("role", u.getRol().name(), "name", u.getNombre()));
        return respondWithToken(token);
    }

    public static record LoginReq(String email, String password) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginReq req) {
        String token = authService.login(req.email(), req.password());
        return respondWithToken(token);
    }

    private ResponseEntity<?> respondWithToken(String token) {
    log.info("Issuing auth token of length {}", token != null ? token.length() : 0);
        ResponseCookie cookie = ResponseCookie.from("AUTH", token)
                .path("/")
                .sameSite("Lax")
                .httpOnly(false)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("token", token));
    }

    // Ayuda: si acceden por GET a /api/auth/login o /api/auth/register, redirige a las páginas HTML
    @GetMapping("/login")
    public ResponseEntity<?> loginGet() {
        log.debug("GET /api/auth/login -> redirecting to /login");
        return ResponseEntity.status(302).header("Location", "/login").build();
    }
    @GetMapping("/register")
    public ResponseEntity<?> registerGet() {
        log.debug("GET /api/auth/register -> redirecting to /register");
        return ResponseEntity.status(302).header("Location", "/register").build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(org.springframework.security.core.Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(Map.of("principal", auth.getName(), "authorities", auth.getAuthorities()));
    }
}
