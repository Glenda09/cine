package com.cine.config;

import com.cine.security.HtmlRedirectEntryPoint;
import com.cine.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;
    private final HtmlRedirectEntryPoint htmlRedirectEntryPoint;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, HtmlRedirectEntryPoint htmlRedirectEntryPoint) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.htmlRedirectEntryPoint = htmlRedirectEntryPoint;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/css/**", "/js/**", "/images/**",
                                 "/peliculas/**", "/funcion/**",
                                 "/api/auth/**",
                                 "/api/funciones",
                                 "/webhooks/wompi",
                                 "/checkout/return",
                                 "/mis-compras",
                                 "/login", "/register", "/logout",
                                 "/tickets/**").permitAll()
                .requestMatchers("/api/reservas/**", "/checkout/start/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .exceptionHandling(e -> e.authenticationEntryPoint(htmlRedirectEntryPoint))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}