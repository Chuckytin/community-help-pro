package com.communityhelp.app.donation.service;

import com.communityhelp.app.chat.conversation.model.ConversationType;
import com.communityhelp.app.chat.conversation.service.ConversationService;
import com.communityhelp.app.donation.dto.DonationCreateRequestDto;
import com.communityhelp.app.donation.dto.DonationResponseDto;
import com.communityhelp.app.donation.dto.DonationUpdateRequestDto;
import com.communityhelp.app.donation.event.DonationCreatedEvent;
import com.communityhelp.app.donation.event.DonationUpdatedEvent;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

        Donation savedDonation = donationRepository.save(donation);

        // Dispara la generación automática de Donation
        eventPublisher.publishEvent(
                new DonationCreatedEvent(savedDonation.getId())
        );

        return donationMapper.toDto(savedDonation);

    }

    @Override
    @Transactional(readOnly = true)
    public DonationResponseDto getDonationById(UUID id) {

        return donationMapper.toDto(getById(id));

    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationResponseDto> getMyDonations(UUID donorId) {

        return donationRepository.findByDonor_Id(donorId)
                .stream()
                .peek(this::autoExpireIfNeeded)
                .map(donationMapper::toDto)
                .collect(Collectors.toList());

    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationResponseDto> getDonationsByStatus(UUID donorId, DonationStatus status) {

        return donationRepository.findByDonor_IdAndStatus(donorId, status)
                .stream()
                .peek(this::autoExpireIfNeeded)
                .map(donationMapper::toDto)
                .collect(Collectors.toList());

    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationResponseDto> getDonationsAssignedToVolunteer(UUID volunteerId) {

        return donationRepository.findByVolunteer_Id(volunteerId)
                .stream()
                .map(donationMapper::toDto)
                .collect(Collectors.toList());

    }

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

        donation.setStatus(DonationStatus.CANCELLED);
        donation.setVolunteer(null);
        donation.setActive(false);

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
