package com.communityhelp.app.chat.conversation.dto;

import com.communityhelp.app.chat.conversation.model.ConversationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO que representa una conversación enviada al frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponseDto {

    private UUID id;

    private ConversationType type;

    private UUID relatedEntityId;

    /**
     * Participantes de la conversación
     */
    private List<ParticipantDto> participants;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * DTO interno para representar participantes
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantDto {

        private UUID userId;
        private String userName;
    }

}
