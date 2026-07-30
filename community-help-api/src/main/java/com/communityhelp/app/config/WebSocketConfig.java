package com.communityhelp.app.config;

import com.communityhelp.app.chat.websocket.config.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración principal de WebSocket con STOMP.
 * - Define los prefijos de routing.
 * - Registra el endpoint /ws.
 * - Añade el interceptor para autenticación JWT en el handshake.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor authInterceptor;

    @Value("${url.frontend.base}")
    private String frontendUrl;

    /**
     * Configura el broker interno de mensajes.
     * /topic - Servidor hace broadcast a clientes
     * /queue - Comunicación privada (uno a uno)
     * /app - Cliente envía mensajes al servidor
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // Broker interno simple
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefijo para mensajes enviados desde cliente
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Registra el endpoint WebSocket principal.
     * Añade el WebSocketAuthInterceptor para validar JWT antes de establecer la conexión.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .addInterceptors(authInterceptor)
                .setAllowedOrigins(frontendUrl);
//                .setAllowedOriginPatterns("*");
//                .withSockJS();
    }

}
