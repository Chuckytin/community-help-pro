package com.communityhelp.app.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Maneja la excepción 401 cuando un usuario no autenticado intenta acceder a un endpoint protegido.
 * - Devuelve un JSON con el mensaje de error.
 * - Se configura en SecurityConfig para que se use en caso de autenticación fallida.
 */
@Component
public class CustomAuthenticationEntryPoint implements org.springframework.security.web.AuthenticationEntryPoint {

    @Override
    public void commence(@NonNull HttpServletRequest request,
                         HttpServletResponse response,
                         @NonNull AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        response.getWriter().write("""
                    {
                      "status": 401,
                      "message": "Authentication required"
                    }
                """);
    }
}