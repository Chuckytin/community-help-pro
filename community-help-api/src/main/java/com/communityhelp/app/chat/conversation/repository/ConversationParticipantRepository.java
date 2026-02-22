package com.communityhelp.app.chat.conversation.repository;

import com.communityhelp.app.chat.conversation.model.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, UUID> {

    /**
     * Comprueba si un usuario ya es participante de una conversación
     */
    Optional<ConversationParticipant> findByConversation_IdAndUser_Id(UUID conversationId, UUID userId);

    /**
     * Devuelve todos los participantes de una conversación
     */
    List<ConversationParticipant> findByConversation_Id(UUID conversationId);

}
