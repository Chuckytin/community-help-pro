package com.communityhelp.app.auth.oauth2;

import com.communityhelp.app.auth.service.JwtService;
import com.communityhelp.app.user.model.Role;
import com.communityhelp.app.user.model.User;
import com.communityhelp.app.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Se ejecuta cuando Google autentica al usuario correctamente y redirige al backend.
 * Responsabilidades:
 * 1. Buscar si el usuario ya existe en la BBDD por email
 * 2. Si no existe, crearlo con los datos de Google (sin contraseña)
 * 3. Generar un JWT propio de la app
 * 4. Redirigir al frontend con el JWT en la URL
 */
@Component
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    /**
     * @Lazy rompe el ciclo de dependencias:
     * SecurityConfig - OAuth2SuccessHandler - AuthenticationService - SecurityConfig
     * Al ser lazy, AuthenticationService no se instancia hasta que se necesita
     * por primera vez, cuando SecurityConfig ya está completamente inicializado.
     */
    public OAuth2SuccessHandler(
            UserRepository userRepository,
            @Lazy JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Value("${url.frontend.oauth2-success}")
    private String frontendOAuth2SuccessUrl;

    /**
     * OAuth2User contiene los datos que devuelve Google (email, name, picture...)
     * Busca el usuario o lo crea si es la primera vez que entra con Google.
     * Genera el JWT usando el UserDetails del usuario.
     * Redirige al frontend con el token en la URL como query param
     * El frontend lo leerá en /oauth2/callback?token=eyJ...
     */
    @Override
    public void onAuthenticationSuccess(
            @NonNull HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        assert oAuth2User != null;
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        Boolean emailVerified = oAuth2User.getAttribute("email_verified");

        log.info("[OAuth2] Login con Google: email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createOAuth2User(email, name, emailVerified));

        org.springframework.security.core.userdetails.UserDetails userDetails =
                new com.communityhelp.app.security.AppUserDetails(user);
        String token = jwtService.generateToken(userDetails);

        log.info("[OAuth2] JWT generado para usuario: {}", email);

        String redirectUrl = frontendOAuth2SuccessUrl + "?token=" + token;
        response.sendRedirect(redirectUrl);
    }

    /**
     * Crea un nuevo usuario en la BBDD con los datos de Google.
     * - Sin contraseña (no puede hacer login con email/password)
     * - Email ya verificado (Google lo garantiza)
     * - Rol USER por defecto
     */
    private User createOAuth2User(String email, String name, Boolean emailVerified) {
        log.info("[OAuth2] Creando nuevo usuario desde Google: email={}", email);

        User user = User.builder()
                .email(email)
                .name(name)
                .passwordHash(null)
                .role(Role.USER)
                .emailVerified(emailVerified != null && emailVerified)
                .active(true)
                .build();

        return userRepository.save(user);
    }
}