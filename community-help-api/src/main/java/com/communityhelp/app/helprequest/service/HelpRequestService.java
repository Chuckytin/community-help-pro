package com.communityhelp.app.helprequest.service;

import com.communityhelp.app.helprequest.dto.HelpRequestCreateRequestDto;
import com.communityhelp.app.helprequest.dto.HelpRequestResponseDto;
import com.communityhelp.app.helprequest.dto.HelpRequestUpdateRequestDto;
import com.communityhelp.app.helprequest.model.HelpRequestStatus;

import java.util.List;
import java.util.UUID;

public interface HelpRequestService {

    // CRUD base
    HelpRequestResponseDto createHelpRequest(UUID requesterId, HelpRequestCreateRequestDto dto);

    HelpRequestResponseDto getHelpRequestById(UUID id);

    List<HelpRequestResponseDto> getMyHelpRequests(UUID requesterId);

    List<HelpRequestResponseDto> getAssignedToVolunteer(UUID volunteerId);

    List<HelpRequestResponseDto> getByStatus(HelpRequestStatus status);

    List<HelpRequestResponseDto> getByVolunteerAndStatus(UUID volunteerId, HelpRequestStatus status);

    HelpRequestResponseDto updateHelpRequest(UUID id, UUID requesterId, HelpRequestUpdateRequestDto dto);

    void deleteHelpRequest(UUID id, UUID requesterId);

    void deleteHelpRequestAsAdmin(UUID id);

    // acciones de negocio
    HelpRequestResponseDto acceptHelpRequest(UUID helpRequestId, UUID volunteerUserId);

    HelpRequestResponseDto completeHelpRequest(UUID helpRequestId, UUID volunteerUserId);

    HelpRequestResponseDto cancelHelpRequest(UUID helpRequestId, UUID requesterId);
}

