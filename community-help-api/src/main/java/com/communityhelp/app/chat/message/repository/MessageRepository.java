package com.communityhelp.app.chat.message.repository;

import com.communityhelp.app.chat.message.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    /**
     * Mensajes paginados de una conversación ordenados por fecha ascendente.
     * EntityGraph para evitar N+1 con sender.
     */
    @EntityGraph(attributePaths = {"sender"})
    Page<Message> findByConversation_IdOrderBySentAtAsc(UUID conversationId, Pageable pageable);

    /**
     * Encuentra el mensaje que perteneza a la conversación.
     */
    Optional<Message> findByIdAndConversation_Id(UUID messageId, UUID conversationId);

}
