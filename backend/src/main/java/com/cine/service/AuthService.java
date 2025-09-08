package com.cine.service;

import com.cine.domain.Usuario;
import com.cine.repo.UsuarioRepository;
import com.cine.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.cine.domain.Enums.RolUsuario;

@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UsuarioRepository usuarioRepository, JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.jwtUtil = jwtUtil;
    }

    public Usuario register(String nombre, String email, String rawPassword, RolUsuario rol) {
        Usuario u = Usuario.builder()
                .nombre(nombre)
                .email(email)
                .hashPassword(encoder.encode(rawPassword))
                .rol(rol)
                .build();
        return usuarioRepository.save(u);
    }

    public String login(String email, String rawPassword) {
        Usuario u = usuarioRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (!encoder.matches(rawPassword, u.getHashPassword())) throw new RuntimeException("Credenciales inválidas");
        return jwtUtil.generateToken(u.getEmail(), Map.of("role", u.getRol().name(), "name", u.getNombre()));
    }
}

