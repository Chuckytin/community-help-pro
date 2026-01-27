package com.communityhelp.app.conversation.repository;

import com.communityhelp.app.conversation.model.Conversation;
import com.communityhelp.app.conversation.model.ConversationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    /**
     * Obtiene todas las conversaciones por el tipo de conversación y la entidad relacionada.
     * Recupera la conversación asociada a una Donation o HelpRequest.
     */
    Page<Conversation> findByTypeAndRelatedEntityId(
            ConversationType type,
            UUID relatedEntityId,
            Pageable pageable
    );

}
