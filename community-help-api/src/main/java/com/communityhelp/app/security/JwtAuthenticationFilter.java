package com.communityhelp.app.security;

import com.communityhelp.app.auth.service.AuthenticationService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro JWT que se ejecuta una vez por request.
 * - Extrae el token JWT que se ejecuta una vez por request.
 * - Valida el token y obtiene los datos del usuario.
 * - Establece la autenticación en el SecurityContext.
 * - Si el token es inválido propaga la excepción para que sea manejada por GlobalExceptionHandler
 */
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationService authenticationService;

    /**
     * Procesa cada request HTTP para comprobar si incluye un token JWT válido.
     * Si el token es válido:
     * - Se crea un UsernamePasswordAuthenticationToken.
     * - Se guarda en el SecurityContextHolder.
     * - Se añade el userId al request si el UserDetails lo soporta.
     * Si el token es inválido o expirado, propaga la excepción.
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            UserDetails userDetails = authenticationService.validateToken(token);

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            if (userDetails instanceof AppUserDetails appUser) {
                request.setAttribute("userId", appUser.getId());
            }

        } catch (ExpiredJwtException e) {
            log.warn("[security][JwtAuthenticationFilter] Expired JWT: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            sendUnauthorized(response, "Token expired");
            return;
        } catch (Exception e) {
            log.warn("[security][JwtAuthenticationFilter] Invalid JWT: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            sendUnauthorized(response, "Invalid Token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT del header Authorization sin el prefijo "Bearer "
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Envía una respuesta 401 Unauthorized con un mensaje de error en formato JSON.
     */
    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"error\": \"UNAUTHORIZED\", \"message\": \"" + message + "\"}"
        );
    }

}
