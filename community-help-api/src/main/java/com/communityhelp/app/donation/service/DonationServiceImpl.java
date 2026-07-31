package com.communityhelp.app.donation.service;

import com.communityhelp.app.chat.conversation.model.ConversationType;
import com.communityhelp.app.chat.conversation.service.ConversationService;
import com.communityhelp.app.common.openroute.dto.FastestTravelResponse;
import com.communityhelp.app.common.openroute.dto.TravelTimeResponse;
import com.communityhelp.app.common.openroute.model.TransportMode;
import com.communityhelp.app.common.openroute.service.TravelFeasibilityService;
import com.communityhelp.app.donation.dto.DonationCreateRequestDto;
import com.communityhelp.app.donation.dto.DonationResponseDto;
import com.communityhelp.app.donation.dto.DonationUpdateRequestDto;
import com.communityhelp.app.donation.event.DonationCreatedEvent;
import com.communityhelp.app.donation.event.DonationUpdatedEvent;
import com.communityhelp.app.donation.exception.DuplicateDonationException;
import com.communityhelp.app.donation.mapper.DonationMapper;
import com.communityhelp.app.donation.model.Donation;
import com.communityhelp.app.donation.model.DonationStatus;
import com.communityhelp.app.donation.repository.DonationRepository;
import com.communityhelp.app.proposal.matching.service.ProposalMatchingStateService;
import com.communityhelp.app.user.model.User;
import com.communityhelp.app.user.repository.UserRepository;
import com.communityhelp.app.volunteer.model.Volunteer;
import com.communityhelp.app.volunteer.repository.VolunteerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DonationServiceImpl implements DonationService {

    private final DonationRepository donationRepository;
    private final DonationMapper donationMapper;
    private final UserRepository userRepository;
    private final VolunteerRepository volunteerRepository;
    private final ConversationService conversationService;
    private final ApplicationEventPublisher eventPublisher;
    private final ProposalMatchingStateService matchingStateService;
    private final TravelFeasibilityService travelFeasibilityService;

    /**
     * Crea una nueva donación asociada al donor indicado, con estado inicial AVAILABLE.
     * - Si el DTO incluye latitud y longitud, se guarda la ubicación geográfica de la donación.
     * - Si el donor ya tiene una donación activa con el mismo título, lanza una excepción para evitar duplicados.
     * - Dispara un evento de creación de donación para que otros componentes puedan reaccionar.
     * - Devuelve los datos de la donación creada en la respuesta.
     * Nota: La lógica de expiración automática se maneja al leer la donación, no al crearla, para evitar procesos en background adicionales.
     */
    @Override
    public DonationResponseDto createDonation(UUID donorId, DonationCreateRequestDto dto) {

        User donor = userRepository.findById(donorId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + donorId));

        Donation donation = Donation.builder()
                .donor(donor)
                .donationType(dto.getDonationType())
                .foodType(dto.getFoodType())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .quantity(dto.getQuantity())
                .unit(dto.getUnit())
                .expiryDate(dto.getExpiryDate())
                .status(DonationStatus.AVAILABLE)
                .build();

        // Set location desde latitude/longitude si vienen en el DTO
        if (dto.getLatitude() != null && dto.getLongitude() != null) {
            donation.setLocation(dto.getLatitude(), dto.getLongitude());
        }

        if (donationRepository.existsByDonor_IdAndTitleIgnoreCaseAndStatus(
                donorId, dto.getTitle(), DonationStatus.AVAILABLE)) {
            throw new DuplicateDonationException();
        }

        Donation savedDonation = donationRepository.save(donation);

        // Dispara la generación automática de Donation
        eventPublisher.publishEvent(
                new DonationCreatedEvent(savedDonation.getId())
        );

        return donationMapper.toDto(savedDonation);

    }

    /**
     * Obtiene una donación por su ID, incluyendo estimaciones de viaje si el usuario actual tiene ubicación.
     */
    @Override
    @Transactional(readOnly = true)
    public DonationResponseDto getDonationById(UUID id, UUID currentUserId) {

        Donation donation = getById(id);
        DonationResponseDto dto = donationMapper.toDto(donation);

        // Solo calcula si ambos tienen ubicación
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (currentUser.getLocation() != null && donation.getLocation() != null) {

            // Estimación con el modo del voluntario
            TransportMode mode = (currentUser.getVolunteer() != null
                    && currentUser.getVolunteer().getTransportMode() != null)
                    ? currentUser.getVolunteer().getTransportMode()
                    : TransportMode.FOOT_WALKING;

            TravelTimeResponse travel = travelFeasibilityService.getEstimatedTravel(
                    currentUser.getLocation(), donation.getLocation(), mode);
            dto.setEstimatedTravelSeconds(travel.getDuration());
            dto.setEstimatedDistanceMeters(travel.getDistance());
            dto.setUsedTransportMode(mode);

            // Estimación del más rápido
            FastestTravelResponse fastest = travelFeasibilityService.getFastestTravel(
                    currentUser.getLocation(), donation.getLocation());
            dto.setFastestTravelSeconds(fastest.getDuration());
            dto.setFastestDistanceMeters(fastest.getDistance());
            dto.setFastestTransportMode(fastest.getFastestMode());
        }

        return dto;
    }

    /**
     * Obtiene las donaciones de un usuario sin importar el estado, ordenadas por fecha de creación descendente.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<DonationResponseDto> getMyDonations(UUID donorId, int page, int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 50));

        return donationRepository.findByDonor_Id(donorId, pageable)
                .map(donation -> {
                    autoExpireIfNeeded(donation);
                    return donationMapper.toDto(donation);
                });

    }

    /**
     * Obtiene las donaciones de un usuario filtrando por estado.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<DonationResponseDto> getDonationsByStatus(UUID donorId, DonationStatus status, int page, int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 50));

        return donationRepository.findByDonor_IdAndStatus(donorId, status, pageable)
                .map(donation -> {
                    autoExpireIfNeeded(donation);
                    return donationMapper.toDto(donation);
                });

    }

    /**
     * Obtiene las donaciones asignadas a un voluntario, sin importar el estado.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<DonationResponseDto> getDonationsAssignedToVolunteer(UUID volunteerId, int page, int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 50));

        return donationRepository.findByVolunteer_Id(volunteerId, pageable)
                .map(donation -> {
                    autoExpireIfNeeded(donation);
                    return donationMapper.toDto(donation);
                });

    }

    /**
     * Busca donaciones disponibles cercanas con PostGIS, opcionalmente filtrando por tipo de donación.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<DonationResponseDto> findNearby(UUID currentUserId, double lat, double lon,
                                                double radiusMeters, String donationType,
                                                int page, int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 50));

        Page<Donation> donations = donationRepository.findNearbyAvailable(
                lat, lon, radiusMeters, donationType, pageable);

        // Obtiene la ubicación del usuario para calcular el tiempo de viaje
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return donations.map(donation -> {
            autoExpireIfNeeded(donation);
            DonationResponseDto dto = donationMapper.toDto(donation);

            if (currentUser.getLocation() != null && donation.getLocation() != null) {
                TransportMode mode = (currentUser.getVolunteer() != null
                        && currentUser.getVolunteer().getTransportMode() != null)
                        ? currentUser.getVolunteer().getTransportMode()
                        : TransportMode.FOOT_WALKING;

                TravelTimeResponse travel = travelFeasibilityService.getEstimatedTravel(
                        currentUser.getLocation(), donation.getLocation(), mode);
                dto.setEstimatedTravelSeconds(travel.getDuration());
                dto.setEstimatedDistanceMeters(travel.getDistance());
                dto.setUsedTransportMode(mode);

                FastestTravelResponse fastest = travelFeasibilityService.getFastestTravel(
                        currentUser.getLocation(), donation.getLocation());
                dto.setFastestTravelSeconds(fastest.getDuration());
                dto.setFastestDistanceMeters(fastest.getDistance());
                dto.setFastestTransportMode(fastest.getFastestMode());
            }

            return dto;
        });
    }

    /**
     * Actualiza una donación existente. Solo se pueden actualizar las donaciones en estado AVAILABLE y solo por el donor propietario.
     */
    @Override
    public DonationResponseDto updateDonation(UUID id, UUID donorId, DonationUpdateRequestDto dto) {

        Donation donation = getOwnedDonation(id, donorId);

        autoExpireIfNeeded(donation);

        if (donation.getStatus() != DonationStatus.AVAILABLE) {
            throw new IllegalStateException("Only AVAILABLE donations can be updated");
        }

        if (dto.getDonationType() != null) {
            donation.setDonationType(dto.getDonationType());
        }

        if (dto.getTitle() != null) donation.setTitle(dto.getTitle());
        if (dto.getDescription() != null) donation.setDescription(dto.getDescription());
        if (dto.getQuantity() != null) donation.setQuantity(dto.getQuantity());
        if (dto.getUnit() != null) donation.setUnit(dto.getUnit());
        if (dto.getExpiryDate() != null) donation.setExpiryDate(dto.getExpiryDate());

        if (dto.getLatitude() != null && dto.getLongitude() != null) {
            donation.setLocation(dto.getLatitude(), dto.getLongitude());
        }

        Donation savedDonation = donationRepository.save(donation);

        eventPublisher.publishEvent(
                new DonationUpdatedEvent(savedDonation.getId())
        );

        return donationMapper.toDto(donation);
    }

    @Override
    public void deleteDonation(UUID id, UUID donorId) {

        Donation donation = getOwnedDonation(id, donorId);

        donationRepository.delete(donation);
    }

    @Override
    public void deleteDonationAsAdmin(UUID id) {
        donationRepository.deleteById(id);
    }

    // ACCIONES DE NEGOCIO

    @Override
    public DonationResponseDto reserveDonation(UUID id, UUID volunteerId) {

        Donation donation = getById(id);

        autoExpireIfNeeded(donation);

        if (donation.getStatus() != DonationStatus.AVAILABLE) {
            throw new IllegalStateException("Only AVAILABLE donations can be reserved");
        }

        // Verifica que el volunteer no sea el donor
        if (donation.getDonorId().equals(volunteerId)) {
            throw new IllegalStateException("Donor cannot reserve their own donation");
        }

        Volunteer volunteer = volunteerRepository.findByUser_Id(volunteerId)
                .orElseThrow(() -> new IllegalStateException("User is not a volunteer"));

        donation.reserve(volunteer);

        return donationMapper.toDto(donation);
    }

    @Override
    public DonationResponseDto confirmDonation(UUID id, UUID donorId) {

        Donation donation = getOwnedDonation(id, donorId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (donation.getStatus() != DonationStatus.RESERVED) {
            throw new IllegalStateException("Only RESERVED donations can be confirmed");
        }

        // Si hay volunteer asignado, no permite que el mismo donor sea volunteer
        if (donation.getVolunteer() != null && donation.getVolunteer().getId().equals(donorId)) {
            throw new IllegalStateException("Donor cannot confirm as their own volunteer");
        }

        donation.confirm();

        // Crea o recupera la conversación automáticamente
        UUID volunteerId = donation.getVolunteer() != null ? donation.getVolunteer().getId() : null;
        if (volunteerId != null) {
            conversationService.getOrCreateConversation(
                    donation.getId(),
                    ConversationType.DONATION.name(),
                    volunteerId,
                    authentication
            );
        }

        // Asegura que el donor esté en la conversación
        conversationService.getOrCreateConversation(
                donation.getId(),
                ConversationType.DONATION.name(),
                donorId,
                authentication
        );

        return donationMapper.toDto(donation);
    }

    @Override
    public DonationResponseDto pickupDonation(UUID id, UUID volunteerId) {

        Donation donation = getById(id);

        if (donation.getStatus() != DonationStatus.CONFIRMED) {
            throw new IllegalStateException("Donation not ready for pickup");
        }

        if (!donation.getVolunteer().getUserId().equals(volunteerId)) {
            throw new IllegalStateException("Only assigned volunteer can pickup");
        }

        donation.pickedUp();

        return donationMapper.toDto(donation);
    }

    @Override
    public DonationResponseDto completeDonation(UUID id, UUID volunteerId) {

        Donation donation = getById(id);

        if (donation.getStatus() != DonationStatus.PICKED_UP) {
            throw new IllegalStateException("Only PICKED UP donations can be completed");
        }

        if (!donation.getVolunteer().getUserId().equals(volunteerId)) {
            throw new IllegalStateException("Only assigned volunteer can complete this request");
        }

        donation.complete();

        return donationMapper.toDto(donation);
    }

    @Override
    public DonationResponseDto cancelDonation(UUID id, UUID donorId) {

        Donation donation = getOwnedDonation(id, donorId);

        if (donation.getStatus() != DonationStatus.AVAILABLE &&
                donation.getStatus() != DonationStatus.RESERVED) {
            throw new IllegalStateException("Cannot cancel this donation");
        }

        donation.cancel("Cancelled by donor");

        // Limpia el estado de matching
        matchingStateService.clearState(id);

        return donationMapper.toDto(donation);
    }

    /**
     * Helper para encontrar la Donation por su ID
     */
    private Donation getById(UUID id) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Donation not found with ID: " + id));

        autoExpireIfNeeded(donation);

        return donation;

    }

    /**
     * Helper para obtener una Donation y valida que pertenezca al donor indicado.
     * Solo el creador puede modificar o eliminar la solicitud.
     */
    private Donation getOwnedDonation(UUID id, UUID donorId) {
        Donation donation = getById(id);

        if (!donation.getDonorId().equals(donorId)) {
            throw new IllegalStateException("Not allowed");
        }

        return donation;
    }

    /**
     * Marca la solicitud como EXPIRED si:
     * - está AVAILABLE
     * - tiene expiryDate y ya pasó
     */
    private void autoExpireIfNeeded(Donation donation) {

        if (donation.getStatus() == DonationStatus.AVAILABLE
                && donation.getExpiryDate() != null
                && donation.getExpiryDate().isBefore(LocalDateTime.now())) {

            donation.expire();
            matchingStateService.clearState(donation.getId());
        }
    }


}
