package com.communityhelp.app.helprequest.service;

import com.communityhelp.app.helprequest.dto.HelpRequestCreateRequestDto;
import com.communityhelp.app.helprequest.dto.HelpRequestResponseDto;
import com.communityhelp.app.helprequest.dto.HelpRequestUpdateRequestDto;
import com.communityhelp.app.helprequest.model.HelpRequestStatus;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface HelpRequestService {

    // CRUD base
    HelpRequestResponseDto createHelpRequest(UUID requesterId, HelpRequestCreateRequestDto dto);

    HelpRequestResponseDto getHelpRequestById(UUID id);

    Page<HelpRequestResponseDto> getMyHelpRequests(UUID requesterId, int page, int size, HelpRequestStatus status);

    HelpRequestResponseDto getMyHelpRequestById(UUID requesterId, UUID requestId);

    Page<HelpRequestResponseDto> getOpenHelpRequests(int page, int size);

    Page<HelpRequestResponseDto> getAssignedToVolunteer(UUID volunteerId, int page, int size, HelpRequestStatus status);

    Page<HelpRequestResponseDto> getByStatus(HelpRequestStatus status, int page, int size);

    Page<HelpRequestResponseDto> getByVolunteer(UUID volunteerId, int page, int size);

    HelpRequestResponseDto updateHelpRequest(UUID id, UUID requesterId, HelpRequestUpdateRequestDto dto);

    void deleteHelpRequest(UUID id, UUID requesterId);

    void deleteHelpRequestAsAdmin(UUID id);

    // acciones de negocio
    HelpRequestResponseDto acceptHelpRequest(UUID helpRequestId, UUID volunteerUserId);

    HelpRequestResponseDto completeHelpRequest(UUID helpRequestId, UUID volunteerUserId);

    HelpRequestResponseDto cancelHelpRequest(UUID helpRequestId, UUID requesterId);
}

