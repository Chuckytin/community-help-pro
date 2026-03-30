package com.communityhelp.app.helprequest.service;

import com.communityhelp.app.chat.conversation.model.ConversationType;
import com.communityhelp.app.chat.conversation.service.ConversationService;
import com.communityhelp.app.common.openroute.dto.FastestTravelResponse;
import com.communityhelp.app.common.openroute.dto.TravelTimeResponse;
import com.communityhelp.app.common.openroute.model.TransportMode;
import com.communityhelp.app.common.openroute.service.TravelFeasibilityService;
import com.communityhelp.app.helprequest.dto.HelpRequestCreateRequestDto;
import com.communityhelp.app.helprequest.dto.HelpRequestResponseDto;
import com.communityhelp.app.helprequest.dto.HelpRequestUpdateRequestDto;
import com.communityhelp.app.helprequest.event.HelpRequestCreatedEvent;
import com.communityhelp.app.helprequest.event.HelpRequestUpdatedEvent;
import com.communityhelp.app.helprequest.mapper.HelpRequestMapper;
import com.communityhelp.app.helprequest.model.HelpRequest;
import com.communityhelp.app.helprequest.model.HelpRequestStatus;
import com.communityhelp.app.helprequest.repository.HelpRequestRepository;
import com.communityhelp.app.proposal.matching.service.ProposalMatchingStateService;
import com.communityhelp.app.proposal.service.ProposalService;
import com.communityhelp.app.user.model.User;
import com.communityhelp.app.user.repository.UserRepository;
import com.communityhelp.app.volunteer.model.Volunteer;
import com.communityhelp.app.volunteer.repository.VolunteerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class HelpRequestServiceImpl implements HelpRequestService {

    private final HelpRequestRepository helpRequestRepository;
    private final HelpRequestMapper helpRequestMapper;
    private final UserRepository userRepository;
    private final VolunteerRepository volunteerRepository;
    private final ConversationService conversationService;
    private final ProposalService proposalService;
    private final ApplicationEventPublisher eventPublisher;
    private final ProposalMatchingStateService matchingStateService;
    private final TravelFeasibilityService travelFeasibilityService;

    @Override
    public HelpRequestResponseDto createHelpRequest(UUID requesterId, HelpRequestCreateRequestDto dto) {

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

        // Dispara la generación automática de proposals
        eventPublisher.publishEvent(
                new HelpRequestCreatedEvent(savedHelpRequest.getId())
        );

        return helpRequestMapper.toDto(savedHelpRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public HelpRequestResponseDto getHelpRequestById(UUID id) {
        return helpRequestMapper.toDto(getById(id));
    }

    /**
     * Obtiene todas las solicitudes como requester.
     * Posibilidad de filtrar por estado de la solicitud.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<HelpRequestResponseDto> getMyHelpRequests(
            UUID requesterId,
            int page,
            int size,
            HelpRequestStatus status) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 50));

        Page<HelpRequest> myRequests;

        if (status != null) {
            myRequests = helpRequestRepository
                    .findByRequester_IdAndStatus(requesterId, status, pageable);
        } else {
            myRequests = helpRequestRepository
                    .findByRequester_Id(requesterId, pageable);
        }

        return myRequests.map(helpRequestMapper::toDto);
    }

    /**
     * Obtiene una solicitud como requester y valida que pertenezca al usuario autenticado.
     */
    @Override
    @Transactional(readOnly = true)
    public HelpRequestResponseDto getMyHelpRequestById(UUID requesterId, UUID requestId) {
        HelpRequest helpRequest = getOwnedRequest(requestId, requesterId);
        return helpRequestMapper.toDto(helpRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HelpRequestResponseDto> getOpenHelpRequests(UUID currentUserId, int page, int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 50),
                Sort.by("createdAt").descending());

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return helpRequestRepository
                .findByStatusAndDeadlineAfter(HelpRequestStatus.OPEN, LocalDateTime.now(), pageable)
                .map(hr -> {
                    HelpRequestResponseDto dto = helpRequestMapper.toDto(hr);

                    if (currentUser.getLocation() != null && hr.getLocation() != null) {

                        // Modo del voluntario
                        TransportMode mode = (currentUser.getVolunteer() != null
                                && currentUser.getVolunteer().getTransportMode() != null)
                                ? currentUser.getVolunteer().getTransportMode()
                                : TransportMode.FOOT_WALKING;

                        TravelTimeResponse travel = travelFeasibilityService.getEstimatedTravel(
                                currentUser.getLocation(), hr.getLocation(), mode);
                        dto.setEstimatedTravelSeconds(travel.getDuration());
                        dto.setEstimatedDistanceMeters(travel.getDistance());
                        dto.setUsedTransportMode(mode);

                        // Más rápido
                        FastestTravelResponse fastest = travelFeasibilityService.getFastestTravel(
                                currentUser.getLocation(), hr.getLocation());
                        dto.setFastestTravelSeconds(fastest.getDuration());
                        dto.setFastestDistanceMeters(fastest.getDistance());
                        dto.setFastestTransportMode(fastest.getFastestMode());
                    }

                    return dto;
                });
    }

    /**
     * Obtiene todas las solicitudes asignadas al voluntario.
     * Posibilidad de filtrar por estado de la solicitud.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<HelpRequestResponseDto> getAssignedToVolunteer(
            UUID volunteerId,
            int page,
            int size,
            HelpRequestStatus status) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 50));

        Page<HelpRequest> assignedRequests;

        if (status != null) {
            assignedRequests = helpRequestRepository
                    .findByVolunteer_IdAndStatus(volunteerId, status, pageable);
        } else {
            assignedRequests = helpRequestRepository
                    .findByVolunteer_Id(volunteerId, pageable);
        }

        return assignedRequests.map(helpRequestMapper::toDto);
    }

    /**
     * Obtiene todas las HelpRequests por estado.
     * Solo Admin
     */
    @Override
    @Transactional(readOnly = true)
    public Page<HelpRequestResponseDto> getByStatus(HelpRequestStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return helpRequestRepository.findByStatus(status, pageable)
                .map(helpRequestMapper::toDto);
    }

    /**
     * Obtiene las tareas de un voluntario filtradas por estado.
     * Caso típico: "mis tareas activas" o "mi historial".
     */
    @Override
    @Transactional(readOnly = true)
    public Page<HelpRequestResponseDto> getByVolunteer(UUID volunteerId,
                                                       int page,
                                                       int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return helpRequestRepository
                .findByVolunteer_Id(volunteerId, pageable)
                .map(helpRequestMapper::toDto);
    }

    @Override
    public HelpRequestResponseDto updateHelpRequest(UUID id, UUID requesterId, HelpRequestUpdateRequestDto dto) {

        HelpRequest helpRequest = getOwnedRequest(id, requesterId);

        autoExpireIfNeeded(helpRequest);

        if (helpRequest.getStatus() != HelpRequestStatus.OPEN) {
            throw new IllegalStateException("Only OPEN requests can be updated");
        }

        if (dto.getType() != null) {
            helpRequest.setType(dto.getType());
        }

        if (dto.getTitle() != null) helpRequest.setTitle(dto.getTitle());
        if (dto.getDescription() != null) helpRequest.setDescription(dto.getDescription());
        if (dto.getDeadline() != null) helpRequest.setDeadline(dto.getDeadline());

        if (dto.getLatitude() != null && dto.getLongitude() != null) {
            helpRequest.setLocation(dto.getLatitude(), dto.getLongitude());
        }

        HelpRequest savedHelpRequest = helpRequestRepository.save(helpRequest);

        eventPublisher.publishEvent(
                new HelpRequestUpdatedEvent(savedHelpRequest.getId())
        );

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

    // ACCIONES DE NEGOCIO

    @Override
    public HelpRequestResponseDto acceptHelpRequest(UUID helpRequestId, UUID volunteerUserId) {

        HelpRequest helpRequest = getById(helpRequestId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        autoExpireIfNeeded(helpRequest);

        if (helpRequest.getStatus() != HelpRequestStatus.OPEN) {
            throw new IllegalStateException("Only OPEN requests can be accepted");
        }

        // Verifica que el volunteer sea distinto del requester
        if (helpRequest.getRequesterId().equals(volunteerUserId)) {
            throw new IllegalStateException("Requester cannot accept their own help request");
        }

        Volunteer volunteer = volunteerRepository.findByUser_Id(volunteerUserId)
                .orElseThrow(() -> new IllegalStateException("User is not a volunteer"));

        // Asigna el voluntario
        helpRequest.assignVolunteer(volunteer);
        helpRequest.setActive(false);

        // Cancela todas las otras proposals pendientes para esta HelpRequest
        proposalService.cancelOtherProposals(helpRequest.getId(), volunteer.getId());

        // Crea o recupera la conversación automáticamente
        conversationService.getOrCreateConversation(
                helpRequest.getId(),
                ConversationType.HELP_REQUEST.name(),
                volunteerUserId,
                authentication
        );

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

        helpRequest.complete();

        return helpRequestMapper.toDto(helpRequest);
    }

    @Override
    public HelpRequestResponseDto cancelHelpRequest(UUID helpRequestId, UUID requesterId) {

        HelpRequest helpRequest = getOwnedRequest(helpRequestId, requesterId);

        if (helpRequest.getStatus() != HelpRequestStatus.OPEN &&
                helpRequest.getStatus() != HelpRequestStatus.ACCEPTED) {
            throw new IllegalStateException("Cannot cancel this request");
        }

        helpRequest.cancel("Cancelled by requester");

        // Limpia el estado de matching
        matchingStateService.clearState(helpRequestId);

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
            throw new AccessDeniedException("Not allowed");
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
                && helpRequest.getDeadline().isBefore(LocalDateTime.now())) {

            helpRequest.expire();
            matchingStateService.clearState(helpRequest.getId());
        }
    }

}
