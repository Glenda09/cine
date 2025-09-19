package com.cine.security;

import com.cine.domain.Usuario;
import com.cine.repo.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
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
        // Log presence of Authorization header (do not log token value)
        if (StringUtils.hasText(header)) {
            log.debug("Authorization header present for {}: startsWithBearer={}", request.getRequestURI(), header.startsWith("Bearer "));
        } else {
            log.debug("No Authorization header present for {}", request.getRequestURI());
        }
        String token = null;
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            token = header.substring(7);
        } else if (request.getCookies() != null) {
            // Log cookie names only for debugging
            StringBuilder cookieNames = new StringBuilder();
            for (var c : request.getCookies()) {
                if (cookieNames.length() > 0) cookieNames.append(',');
                cookieNames.append(c.getName());
            }
            log.debug("Cookies present for {}: {}", request.getRequestURI(), cookieNames.toString());
            for (var c : request.getCookies()) {
                if ("AUTH".equals(c.getName()) && StringUtils.hasText(c.getValue())) {
                    token = c.getValue();
                    break;
                }
            }
        }
        // Fallback: sometimes servlet container doesn't populate request.getCookies() as expected
        if (!StringUtils.hasText(token)) {
            String cookieHeader = request.getHeader("Cookie");
            if (StringUtils.hasText(cookieHeader)) {
                log.debug("Raw Cookie header for {}: {}", request.getRequestURI(), cookieHeader.replaceAll("(?<=AUTH=)[^;\\s]+", "<redacted>"));
                int idx = cookieHeader.indexOf("AUTH=");
                if (idx >= 0) {
                    int start = idx + "AUTH=".length();
                    int end = cookieHeader.indexOf(';', start);
                    String raw = end > 0 ? cookieHeader.substring(start, end) : cookieHeader.substring(start);
                    raw = raw.trim();
                    if (raw.length() > 0) {
                        token = raw;
                        log.debug("Extracted AUTH cookie value from raw header for {} (length={})", request.getRequestURI(), token.length());
                    }
                }
            }
        }
        if (StringUtils.hasText(token)) {
            log.debug("Found token for request {}: token length={}", request.getRequestURI(), token.length());
            try {
                var claims = jwtUtil.parse(token);
                String email = claims.getSubject();
                Object rawRole = claims.getOrDefault("role", "USER");
                String role = "ROLE_" + (rawRole instanceof String ? (String) rawRole : rawRole.toString());
                log.info("Token parsed for request {}. Subject='{}', roleClaim='{}', granted='{}'", request.getRequestURI(), email, rawRole, role);
                var auth = new UsernamePasswordAuthenticationToken(email, null,
                        List.of(new SimpleGrantedAuthority(role)));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ex) {
                log.warn("Failed to parse/validate JWT token for {}: {}", request.getRequestURI(), ex.getMessage());
            }
        } else {
            log.debug("No Authorization header or AUTH cookie present for {}", request.getRequestURI());
        }
        filterChain.doFilter(request, response);
    }
}
