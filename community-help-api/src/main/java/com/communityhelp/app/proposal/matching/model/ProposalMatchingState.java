package com.communityhelp.app.proposal.matching.model;

import com.communityhelp.app.proposal.model.ProposalType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "proposal_matching_state")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProposalMatchingState {

    @Id
    private UUID entityId;

    @Enumerated(EnumType.STRING)
    private ProposalType entityType;

    private int currentRadiusMeters;
    private int retryCount;
    private LocalDateTime lastRetryAt;

    @Version
    private Long version;
}
