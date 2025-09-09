package com.cine.security;

import com.cine.domain.Usuario;
import com.cine.repo.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthFilter(JwtUtil jwtUtil, UsuarioRepository usuarioRepository) {
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = null;
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            token = header.substring(7);
        } else if (request.getCookies() != null) {
            for (var c : request.getCookies()) {
                if ("AUTH".equals(c.getName()) && StringUtils.hasText(c.getValue())) {
                    token = c.getValue();
                    break;
                }
            }
        }
        if (StringUtils.hasText(token)) {
            try {
                var claims = jwtUtil.parse(token);
                String email = claims.getSubject();
                Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);
                if (userOpt.isPresent()) {
                    String role = "ROLE_" + (String) claims.getOrDefault("role", "USER");
                    var auth = new UsernamePasswordAuthenticationToken(email, null,
                            List.of(new SimpleGrantedAuthority(role)));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception ignored) {}
        }
        filterChain.doFilter(request, response);
    }
}
