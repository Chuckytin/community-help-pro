package com.communityhelp.app.helprequest.service;

import com.communityhelp.app.helprequest.dto.HelpRequestCreateRequestDto;
import com.communityhelp.app.helprequest.dto.HelpRequestResponseDto;
import com.communityhelp.app.helprequest.dto.HelpRequestUpdateRequestDto;
import com.communityhelp.app.helprequest.mapper.HelpRequestMapper;
import com.communityhelp.app.helprequest.model.HelpRequest;
import com.communityhelp.app.helprequest.model.HelpRequestStatus;
import com.communityhelp.app.helprequest.repository.HelpRequestRepository;
import com.communityhelp.app.user.model.User;
import com.communityhelp.app.user.repository.UserRepository;
import com.communityhelp.app.volunteer.repository.VolunteerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HelpRequestServiceImpl implements HelpRequestService {

    private final HelpRequestRepository helpRequestRepository;
    private final HelpRequestMapper helpRequestMapper;
    private final UserRepository userRepository;
    private final VolunteerRepository volunteerRepository;

    @Override
    public HelpRequestResponseDto createHelpRequest(UUID requesterId, HelpRequestCreateRequestDto dto) {

        if (!userRepository.existsById(requesterId)) {
            throw new EntityNotFoundException("User not found with ID: " + requesterId);
        }

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + requesterId));

        HelpRequest helpRequest = HelpRequest.builder()
                .type(dto.getType())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .deadline(dto.getDeadline())
                .requester(requester)
                .status(HelpRequestStatus.OPEN)
                .build();

        // Set location desde latitude/longitude si vienen en el DTO
        if (dto.getLatitude() != null && dto.getLongitude() != null) {
            helpRequest.setLocation(dto.getLatitude(), dto.getLongitude());
        }

        HelpRequest savedHelpRequest = helpRequestRepository.save(helpRequest);

        return helpRequestMapper.toDto(savedHelpRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public HelpRequestResponseDto getHelpRequestById(UUID id) {
        return helpRequestMapper.toDto(getById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HelpRequestResponseDto> getMyHelpRequests(UUID requesterId) {

        return helpRequestRepository.findByRequester_Id(requesterId)
                .stream()
                .peek(this::autoExpireIfNeeded)
                .map(helpRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public HelpRequestResponseDto updateHelpRequest(UUID id, UUID requesterId, HelpRequestUpdateRequestDto dto) {

        HelpRequest helpRequest = getOwnedRequest(id, requesterId);

        autoExpireIfNeeded(helpRequest);

        if (helpRequest.getStatus() != HelpRequestStatus.OPEN) {
            throw new IllegalStateException("Only OPEN requests can be updated");
        }

        if (dto.getTitle() != null) helpRequest.setTitle(dto.getTitle());
        if (dto.getDescription() != null) helpRequest.setDescription(dto.getDescription());
        if (dto.getDeadline() != null) helpRequest.setDeadline(dto.getDeadline());

        if (dto.getLatitude() != null && dto.getLongitude() != null) {
            helpRequest.setLocation(dto.getLatitude(), dto.getLongitude());
        }

        return helpRequestMapper.toDto(helpRequest);
    }

    @Override
    public void deleteHelpRequest(UUID id, UUID requesterId) {

        HelpRequest helpRequest = getOwnedRequest(id, requesterId);

        helpRequestRepository.delete(helpRequest);
    }

    @Override
    public void deleteHelpRequestAsAdmin(UUID id) {
        helpRequestRepository.deleteById(id);
    }

    @Override
    public HelpRequestResponseDto acceptHelpRequest(UUID helpRequestId, UUID volunteerUserId) {

        HelpRequest helpRequest = getById(helpRequestId);

        autoExpireIfNeeded(helpRequest);

        if (helpRequest.getStatus() != HelpRequestStatus.OPEN) {
            throw new IllegalStateException("Only OPEN requests can be accepted");
        }

        var volunteer = volunteerRepository.findByUser_Id(volunteerUserId)
                .orElseThrow(() -> new IllegalStateException("User is not a volunteer"));

        helpRequest.setVolunteer(volunteer);
        helpRequest.setStatus(HelpRequestStatus.ACCEPTED);
        helpRequest.setAcceptedAt(java.time.LocalDateTime.now());

        return helpRequestMapper.toDto(helpRequest);
    }

    @Override
    public HelpRequestResponseDto completeHelpRequest(UUID helpRequestId, UUID volunteerUserId) {

        HelpRequest helpRequest = getById(helpRequestId);

        if (helpRequest.getStatus() != HelpRequestStatus.ACCEPTED) {
            throw new IllegalStateException("Only ACCEPTED requests can be completed");
        }

        if (!helpRequest.getVolunteer().getUserId().equals(volunteerUserId)) {
            throw new IllegalStateException("Only assigned volunteer can complete this request");
        }

        helpRequest.setStatus(HelpRequestStatus.COMPLETED);
        helpRequest.setCompletedAt(java.time.LocalDateTime.now());

        return helpRequestMapper.toDto(helpRequest);
    }

    @Override
    public HelpRequestResponseDto cancelHelpRequest(UUID helpRequestId, UUID requesterId) {

        HelpRequest helpRequest = getOwnedRequest(helpRequestId, requesterId);

        if (helpRequest.getStatus() != HelpRequestStatus.OPEN &&
                helpRequest.getStatus() != HelpRequestStatus.ACCEPTED) {
            throw new IllegalStateException("Cannot cancel this request");
        }

        helpRequest.setStatus(HelpRequestStatus.CANCELLED);

        return helpRequestMapper.toDto(helpRequest);
    }

    /**
     * Helper para encontrar la HelpRequest por su ID
     */
    private HelpRequest getById(UUID id) {
        HelpRequest helpRequest = helpRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("HelpRequest not found with ID: " + id));

        autoExpireIfNeeded(helpRequest);

        return helpRequest;

    }

    /**
     * Helper para obtener una HelpRequest y valida que pertenezca al requester indicado.
     * Solo el creador puede modificar o eliminar la solicitud.
     */
    private HelpRequest getOwnedRequest(UUID id, UUID requesterId) {
        HelpRequest helpRequest = getById(id);

        if (!helpRequest.getRequesterId().equals(requesterId)) {
            throw new IllegalStateException("Not allowed");
        }

        return helpRequest;
    }

    /**
     * Marca la solicitud como EXPIRED si:
     * - está OPEN
     * - tiene deadline y ya pasó
     */
    private void autoExpireIfNeeded(HelpRequest helpRequest) {

        if (helpRequest.getStatus() == HelpRequestStatus.OPEN
                && helpRequest.getDeadline() != null
                && helpRequest.getDeadline().isBefore(java.time.LocalDateTime.now())) {

            helpRequest.setStatus(HelpRequestStatus.EXPIRED);
        }
    }


}
