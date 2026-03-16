package com.communityhelp.app.proposal.mapper;

import com.communityhelp.app.proposal.dto.ProposalResponseDto;
import com.communityhelp.app.proposal.model.Proposal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProposalMapper {

    @Mapping(target = "volunteerId", source = "volunteer.id")
    @Mapping(target = "volunteerName", source = "volunteer.user.name")
    ProposalResponseDto toDto(Proposal proposal);
}