package com.communityhelp.app.chat.websocket.controller;

import com.communityhelp.app.chat.conversation.service.ConversationService;
import com.communityhelp.app.chat.message.dto.MessageCreateRequestDto;
import com.communityhelp.app.chat.message.dto.MessageResponseDto;
import com.communityhelp.app.chat.websocket.dto.ChatMessageWsDto;
import com.communityhelp.app.chat.websocket.dto.TypingWsDto;
import com.communityhelp.app.security.AppUserDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.Objects;
import java.util.UUID;

/**
 * Controlador WebSocket para mensajería en tiempo real.
 * Maneja envío de mensajes y eventos de escritura (typing).
 * Obtiene el usuario autenticado desde la sesión WebSocket.
 */
@Tag(
        name = "Chat",
        description = """
        REST messaging API and real-time WebSocket protocol.
        
        **WebSocket connection:**
        - URL: `ws://localhost:8080/ws`
        - Protocol: STOMP over WebSocket
        - Auth: send JWT token as query param → `/ws?token=YOUR_JWT`
        
        **Send a message:**
        - Destination: `/app/chat.sendMessage`
        - Body: `{ "conversationId": "uuid", "content": "text" }`
        - Subscribe to receive: `/topic/conversations/{conversationId}`
        
        **Typing indicator:**
        - Destination: `/app/chat.typing`
        - Body: `{ "conversationId": "uuid" }`
        - Subscribe to receive: `/topic/conversations/{conversationId}/typing`
        """
)
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ConversationService conversationService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Procesa el envío de un mensaje en una conversación.
     * - Obtiene el usuario autenticado.
     * - Guarda el mensaje en base de datos.
     * - Envía el mensaje guardado a todos los suscritos
     *   en /topic/conversations/{conversationId}.
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(ChatMessageWsDto dto,
                            SimpMessageHeaderAccessor headerAccessor) {

        Authentication authentication = (Authentication) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("user");

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

        UUID senderId = Objects.requireNonNull(userDetails).getId();

        MessageResponseDto saved = conversationService.sendMessage(
                dto.getConversationId(),
                senderId,
                new MessageCreateRequestDto(dto.getContent()),
                authentication
        );

        messagingTemplate.convertAndSend(
                "/topic/conversations/" + dto.getConversationId(),
                saved
        );
    }

    /**
     * Notifica que un usuario está escribiendo en una conversación.
     * Envía el userId a:
     * /topic/conversations/{conversationId}/typing
     */
    @MessageMapping("/chat.typing")
    public void typing(TypingWsDto dto,
                       SimpMessageHeaderAccessor headerAccessor) {

        Authentication authentication = (Authentication) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("user");

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

        UUID userId = Objects.requireNonNull(userDetails).getId();

        messagingTemplate.convertAndSend(
                "/topic/conversations/" + dto.getConversationId() + "/typing",
                userId
        );
    }

}