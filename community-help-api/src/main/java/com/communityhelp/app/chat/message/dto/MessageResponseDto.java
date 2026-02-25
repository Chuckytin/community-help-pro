package com.communityhelp.app.chat.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO que se enviará al frontend con la información del mensaje.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDto {

    private UUID id;

    private UUID conversationId;

    private UUID senderId;
    private String senderName;

    private String content;

    private LocalDateTime sentAt;
}
