package com.cine.config;

import com.cine.domain.Usuario;
import com.cine.repo.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import static com.cine.domain.Enums.RolUsuario;

@Component
@Order(10)
public class DataInit implements CommandLineRunner {
    private final UsuarioRepository usuarioRepository;

    public DataInit(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void run(String... args) {
        var enc = new BCryptPasswordEncoder();
        var admin = usuarioRepository.findByEmail("admin@cine.local").orElse(null);
        if (admin == null) {
            usuarioRepository.save(Usuario.builder()
                    .nombre("Admin")
                    .email("admin@cine.local")
                    .hashPassword(enc.encode("admin123"))
                    .rol(RolUsuario.ADMIN)
                    .build());
        } else {
            admin.setRol(RolUsuario.ADMIN);
            admin.setHashPassword(enc.encode("admin123"));
            usuarioRepository.save(admin);
        }
    }
}
