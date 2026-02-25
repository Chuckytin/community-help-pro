package com.communityhelp.app.chat.websocket.config;

import com.communityhelp.app.chat.websocket.dto.PresenceUpdateDto;
import com.communityhelp.app.chat.websocket.service.PresenceService;
import com.communityhelp.app.security.AppUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Objects;
import java.util.UUID;

/**
 * Listener de eventos de conexión/desconexión WebSocket.
 * - Cuando un usuario se conecta - lo marca como online.
 * - Cuando se desconecta - lo marca como offline.
 * - Notifica a los clientes mediante /topic/presence.
 */
@RequiredArgsConstructor
@Component
public class WebSocketPresenceListener {

    private final PresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Se ejecuta cuando un usuario establece conexión STOMP.
     * Marca al usuario como conectado y notifica presencia.
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        Authentication authentication = (Authentication) Objects.requireNonNull(accessor.getSessionAttributes()).get("user");

        if (authentication == null) return;

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

        UUID userId = Objects.requireNonNull(userDetails).getId();

        presenceService.userConnected(userId);

        messagingTemplate.convertAndSend(
                "/topic/presence",
                new PresenceUpdateDto(userId, true)
        );
    }

    /**
     * Se ejecuta cuando un usuario cierra la conexión.
     * Marca al usuario como desconectado y notifica presencia.
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        Authentication authentication = (Authentication) Objects.requireNonNull(accessor.getSessionAttributes()).get("user");

        if (authentication == null) return;

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

        UUID userId = Objects.requireNonNull(userDetails).getId();

        presenceService.userDisconnected(userId);

        messagingTemplate.convertAndSend(
                "/topic/presence",
                new PresenceUpdateDto(userId, false)
        );
    }
}