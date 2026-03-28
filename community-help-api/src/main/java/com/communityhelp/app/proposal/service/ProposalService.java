package com.communityhelp.app.proposal.service;

import com.communityhelp.app.proposal.dto.ProposalResponseDto;
import com.communityhelp.app.proposal.model.ProposalStatus;
import com.communityhelp.app.proposal.model.ProposalType;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface ProposalService {

    void createProposal(UUID volunteerId, UUID targetEntityId, ProposalType type, double score);

    Page<ProposalResponseDto> getProposalsByVolunteer(UUID volunteerId, int page, int size);

    Page<ProposalResponseDto> getProposalsByStatus(ProposalStatus status, int page, int size);

    ProposalResponseDto getProposalByVolunteerAndId(UUID volunteerId, UUID proposalId);

    ProposalResponseDto getProposalByEntityId(UUID entityId);

    ProposalResponseDto acceptProposal(UUID volunteerId, UUID proposalId);

    ProposalResponseDto rejectProposal(UUID volunteerId, UUID proposalId);

    void cancelOtherProposals(UUID targetEntityId, UUID acceptedVolunteerId);
}
