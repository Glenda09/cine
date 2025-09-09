package com.cine.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HtmlRedirectEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.AuthenticationException authException) throws IOException, ServletException {
        String accept = request.getHeader("Accept");
        String uri = request.getRequestURI();
        boolean wantsHtml = accept != null && accept.contains("text/html");
        boolean isHtmlArea = uri.startsWith("/admin") || uri.equals("/") || uri.startsWith("/peliculas") || uri.startsWith("/funcion");
        if (wantsHtml || isHtmlArea) {
            response.setStatus(302);
            response.setHeader("Location", "/login");
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}

