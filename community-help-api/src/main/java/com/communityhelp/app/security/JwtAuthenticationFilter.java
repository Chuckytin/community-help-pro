package com.communityhelp.app.security;

import com.communityhelp.app.auth.service.JwtService;
import com.communityhelp.app.auth.service.TokenBlacklistService;
import com.communityhelp.app.common.exceptions.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
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

    private final JwtService jwtService;
    private final SecurityResponseWriter securityResponseWriter;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Procesa cada request HTTP para comprobar si incluye un token JWT válido.
     * Si el token es válido:
     * - Se crea un UsernamePasswordAuthenticationToken.
     * - Se guarda en el SecurityContextHolder.
     * - Se añade el userId al request si el UserDetails lo soporta.
     * Si el token es inválido o expirado, propaga la excepción.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (tokenBlacklistService.isBlacklisted(token)) {

            log.warn("[security] Blacklisted JWT used");

            securityResponseWriter.writeSecurityError(
                    response,
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.INVALID_TOKEN,
                    "Token has been invalidated"
            );
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {

            UserDetails userDetails = jwtService.validateToken(token);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            if (userDetails instanceof AppUserDetails appUser) {
                request.setAttribute("userId", appUser.getId());
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {

            log.warn("[security] Expired JWT");

            SecurityContextHolder.clearContext();

            securityResponseWriter.writeSecurityError(
                    response,
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.JWT_EXPIRED,
                    "Token expired"
            );

        } catch (JwtException e) {

            log.warn("[security] Invalid JWT");

            SecurityContextHolder.clearContext();

            securityResponseWriter.writeSecurityError(
                    response,
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.INVALID_TOKEN,
                    "Invalid token"
            );
        }
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

}
