package com.communityhelp.app.security;

import com.communityhelp.app.common.exceptions.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Maneja la excepción 403 cuando un usuario intenta acceder a un endpoint donde no tiene permisos.
 * - Devuelve un JSON con el mensaje de error.
 * - Se configura en SecurityConfig para que se use en caso de autenticación fallida.
 */
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final SecurityResponseWriter securityResponseWriter;

    @Override
    public void handle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull AccessDeniedException exception
    ) throws IOException {

        securityResponseWriter.writeSecurityError(
                response,
                HttpStatus.FORBIDDEN,
                ErrorCode.ACCESS_DENIED,
                "Access denied"
        );
    }
}