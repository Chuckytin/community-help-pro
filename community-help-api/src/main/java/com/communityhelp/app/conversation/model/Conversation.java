package com.communityhelp.app.conversation.model;

import com.communityhelp.app.common.persistence.Auditable;
import com.communityhelp.app.message.model.Message;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "conversations")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Conversation extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Tipo de conversación.
     * - DONATION
     * - HELP_REQUEST
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationType type;

    /**
     * ID de la entidad relacionada (Donation o HelpRequest)
     */
    @Column(name = "related_entity_id", nullable = false)
    private UUID relatedEntityId;

    /**
     * Mensaje de la conversación.
     * LAZY - para evitar cargar todos los mensajes al traer la conversación.
     * Cascade ALL porque todos los mensajes pertenecen a la conversación.
     */
    @OneToMany(
            mappedBy = "conversation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Message> messages = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conversation conversation = (Conversation) o;
        return Objects.equals(id, conversation.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
