package com.communityhelp.app.chat.conversation.repository;

import com.communityhelp.app.chat.conversation.model.Conversation;
import com.communityhelp.app.chat.conversation.model.ConversationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    /**
     * Todas las conversaciones relacionadas con un usuario a través de participants
     */
    @EntityGraph(attributePaths = {"participants", "participants.user"})
    Page<Conversation> findDistinctByParticipants_User_Id(UUID userId, Pageable pageable);

    /**
     * Conversación por tipo y entidad relacionada (ej. Donation o HelpRequest)
     */
    Optional<Conversation> findByTypeAndRelatedEntityId(ConversationType type, UUID relatedEntityId);

}
