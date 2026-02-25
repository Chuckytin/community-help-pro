package com.communityhelp.app.chat.websocket.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresenceUpdateDto {

    private UUID userId;
    private boolean online;
}