package com.communityhelp.app.proposal.dto;

import com.communityhelp.app.proposal.model.ProposalStatus;
import com.communityhelp.app.proposal.model.ProposalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO que representa una propuesta enviada al frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposalResponseDto {

    private UUID id;

    private ProposalType type;

    private UUID targetEntityId;

    private UUID volunteerId;
    private String volunteerName;

    private ProposalStatus status;

    private Double score;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime respondedAt;
}