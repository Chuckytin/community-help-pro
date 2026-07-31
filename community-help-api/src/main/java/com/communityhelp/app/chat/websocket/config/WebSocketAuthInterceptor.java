package com.communityhelp.app.chat.websocket.config;

import com.communityhelp.app.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Interceptor ejecutado antes de establecer el handshake WebSocket.
 * - Extrae el JWT del header Authorization.
 * - Valida el token.
 * - Crea un Authentication.
 * - Lo guarda en los atributos de sesión WebSocket.
 * Si el token es inválido, rechaza la conexión.
 */
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request,
                                   @NonNull ServerHttpResponse response,
                                   @NonNull WebSocketHandler wsHandler,
                                   @NonNull Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest servletRequest) {

            String token = servletRequest.getServletRequest().getParameter("token");

            if (token != null) {
                try {
                    UserDetails userDetails = jwtService.validateToken(token);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    attributes.put("user", authentication);

                    return true;

                } catch (Exception e) {
                    return false;
                }
            }
        }

        return false;
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request,
                               org.springframework.http.server.@NonNull ServerHttpResponse response,
                               org.springframework.web.socket.@NonNull WebSocketHandler wsHandler,
                               Exception exception) {
    }
}