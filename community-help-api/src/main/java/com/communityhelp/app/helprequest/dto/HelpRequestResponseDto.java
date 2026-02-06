package com.communityhelp.app.helprequest.dto;

import com.communityhelp.app.helprequest.model.HelpRequestStatus;
import com.communityhelp.app.helprequest.model.HelpRequestType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta que se enviará al frontend con información de la HelpRequest
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpRequestResponseDto {

    private UUID id;
    private UUID requesterId;
    private UUID volunteerId;
    private HelpRequestType type;
    private String title;
    private String description;
    private Double latitude;
    private Double longitude;
    private LocalDateTime deadline;
    private HelpRequestStatus status;
    private String cancelReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;

}
