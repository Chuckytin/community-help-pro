package com.communityhelp.app.chat.conversation.service;

import com.communityhelp.app.chat.conversation.dto.ConversationResponseDto;
import com.communityhelp.app.chat.message.dto.MessageCreateRequestDto;
import com.communityhelp.app.chat.message.dto.MessageResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public interface ConversationService {

    /**
     * Crea o recupera la conversación para un Donation / HelpRequest
     */
    ConversationResponseDto getOrCreateConversation(UUID relatedEntityId, String type, UUID userId, Authentication authentication);

    /**
     * Obtiene todas las conversaciones de un usuario
     */
    Page<ConversationResponseDto> getUserConversations(UUID userId, int page, int size, Authentication authentication);

    /**
     * Envia un mensaje a la conversación
     */
    MessageResponseDto sendMessage(UUID conversationId, UUID senderId, MessageCreateRequestDto dto, Authentication authentication);

    /**
     * Obtiene todos los mensajes de una conversación
     */
    Page<MessageResponseDto> getMessages(UUID conversationId, UUID userId, int page, int size, Authentication authentication);

    /**
     * Borra el mensaje de la conversación, solo el autor y el admin pueden borrar
     */
    void deleteMessage(UUID conversationId, UUID messageId, UUID currentUserId, Authentication authentication);

    /**
     * Marca la conversación como leída
     */
    void markConversationAsRead(UUID conversationId, UUID userId, Authentication authentication);

}
