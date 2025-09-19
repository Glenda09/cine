package com.cine.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HtmlRedirectEntryPoint implements AuthenticationEntryPoint {
    private static final Logger log = LoggerFactory.getLogger(HtmlRedirectEntryPoint.class);
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.AuthenticationException authException) throws IOException, ServletException {
        String accept = request.getHeader("Accept");
        String uri = request.getRequestURI();
        boolean wantsHtml = accept != null && accept.contains("text/html");
        boolean isHtmlArea = uri.startsWith("/admin") || uri.equals("/") || uri.startsWith("/peliculas") || uri.startsWith("/funcion");
        if (wantsHtml || isHtmlArea) {
            log.info("Unauthenticated HTML request to '{}', Accept='{}'. Redirecting to /login", uri, accept);
            response.setStatus(302);
            response.setHeader("Location", "/login");
        } else {
            log.info("Unauthenticated API request to '{}'. Sending 401", uri);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}

