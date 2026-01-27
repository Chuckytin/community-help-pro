package com.communityhelp.app.message.repository;

import com.communityhelp.app.message.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    /**
     * Obtiene todos los mensajes de una conversación ordenados por fecha de envío descendente.
     * Forma paginada.
     */
    Page<Message> findByConversationIdOrderBySentAtDesc(
            UUID conversationId,
            Pageable pageable
    );

}
