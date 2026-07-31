package com.communityhelp.app.auth.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Se ejecuta cuando falla la autenticación OAuth2 con Google
 * (usuario cancela el consentimiento, error del provider, fallo en el intercambio de token, etc).
 * Redirige al frontend con un parámetro de error en vez de dejar que
 * la petición caiga en el CustomAuthenticationEntryPoint.
 */
@Component
@Slf4j
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    @Value("${url.frontend.login}")
    private String frontendLoginUrl;

    @Override
    public void onAuthenticationFailure(
            @NonNull HttpServletRequest request,
            HttpServletResponse response,
            @NonNull AuthenticationException exception
    ) throws IOException {

        log.warn("[OAuth2] Fallo de autenticación con Google: {}", exception.getMessage());

        String redirectUrl = frontendLoginUrl + "?error=oauth2_failed";
        response.sendRedirect(redirectUrl);
    }
}