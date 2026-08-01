package com.communityhelp.app.security;

import com.communityhelp.app.common.exceptions.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Maneja la excepción 401 cuando un usuario no autenticado intenta acceder a un endpoint protegido.
 * - Devuelve un JSON con el mensaje de error.
 * - Se configura en SecurityConfig para que se use en caso de autenticación fallida.
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final SecurityResponseWriter securityResponseWriter;

    @Override
    public void commence(@NonNull HttpServletRequest request,
                         @NonNull HttpServletResponse response,
                         @NonNull AuthenticationException authException) throws IOException {

        securityResponseWriter.writeSecurityError(
                response,
                HttpStatus.UNAUTHORIZED,
                ErrorCode.AUTHENTICATION_REQUIRED,
                "Authentication required"
        );
    }
}