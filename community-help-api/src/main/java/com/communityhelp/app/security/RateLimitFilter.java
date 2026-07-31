package com.communityhelp.app.security;

import com.communityhelp.app.common.exceptions.ErrorCode;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

/**
 * Filtro de rate limiting para endpoints de autenticación sensibles.
 * Limita las peticiones por IP para proteger contra ataques de fuerza bruta
 * y abuso de los endpoints de registro, verificación y recuperación de contraseña.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final SecurityResponseWriter securityResponseWriter;

    /**
     * Endpoints protegidos y sus límites:
     * - 3 registros por minuto por IP
     * - 3 intentos por minuto por IP para recuperación de contraseña
     * - 10 intentos por minuto por IP para verificación de email
     * - 5 intentos por minuto por IP para reset de contraseña
     */
    private static final Map<String, Integer> RATE_LIMITED_PATHS = Map.of(
            "/api/v1/auth/register", 3,
            "/api/v1/auth/forgot-password", 3,
            "/api/v1/auth/verify-email", 10,
            "/api/v1/auth/reset-password", 5
    );

    /**
     * Caché de buckets por IP + endpoint para aplicar el rate limiting.
     */
    private final Cache<String, Bucket> buckets =
            Caffeine.newBuilder()
                    .expireAfterAccess(Duration.ofMinutes(30))
                    .maximumSize(100_000)
                    .build();

    /**
     * Intercepta las peticiones a los endpoints configurados y aplica el rate limiting
     * Solo aplica a POST en los endpoints configurados
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        String method = request.getMethod();

        if (!"POST".equals(method) || !RATE_LIMITED_PATHS.containsKey(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = getClientIp(request);
        String bucketKey = ip + ":" + path;
        int limit = RATE_LIMITED_PATHS.get(path);

        Bucket bucket = buckets.get(bucketKey, k -> createBucket(limit));

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("[rate-limit] IP {} exceeded limit on {}", ip, path);

            int retryAfterSeconds = 60;
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));

            securityResponseWriter.writeSecurityError(
                    response,
                    HttpStatus.TOO_MANY_REQUESTS,
                    ErrorCode.RATE_LIMIT_EXCEEDED,
                    "Too many requests. Please try again later."
            );
        }
    }

    /**
     * Crea un bucket con el límite de peticiones por minuto indicado.
     */
    private Bucket createBucket(int requestsPerMinute) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(requestsPerMinute)
                .refillGreedy(requestsPerMinute, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Obtiene la IP real del cliente, teniendo en cuenta proxies.
     */
    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}