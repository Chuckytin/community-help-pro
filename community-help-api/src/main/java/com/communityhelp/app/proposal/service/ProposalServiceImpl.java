package com.communityhelp.app.proposal.service;

import com.communityhelp.app.donation.model.Donation;
import com.communityhelp.app.donation.model.DonationStatus;
import com.communityhelp.app.donation.repository.DonationRepository;
import com.communityhelp.app.helprequest.model.HelpRequest;
import com.communityhelp.app.helprequest.model.HelpRequestStatus;
import com.communityhelp.app.helprequest.repository.HelpRequestRepository;
import com.communityhelp.app.proposal.dto.ProposalResponseDto;
import com.communityhelp.app.proposal.mapper.ProposalMapper;
import com.communityhelp.app.proposal.model.Proposal;
import com.communityhelp.app.proposal.model.ProposalStatus;
import com.communityhelp.app.proposal.model.ProposalType;
import com.communityhelp.app.proposal.repository.ProposalRepository;
import com.communityhelp.app.volunteer.model.Volunteer;
import com.communityhelp.app.volunteer.repository.VolunteerRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProposalServiceImpl implements ProposalService {

    private final ProposalRepository proposalRepository;
    private final VolunteerRepository volunteerRepository;
    private final HelpRequestRepository helpRequestRepository;
    private final DonationRepository donationRepository;
    private final ProposalMapper proposalMapper;

    /**
     * Crea una proposal para un voluntario y entidad objetivo.
     * Inicializa score y estado.
     */
    @Override
    public ProposalResponseDto createProposal(UUID volunteerId, UUID targetEntityId, ProposalType type, double score) {

        Volunteer volunteer = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new EntityNotFoundException("Volunteer not found"));

        switch (type) {
            case HELP_REQUEST -> {
                if (!helpRequestRepository.existsById(targetEntityId)) {
                    throw new EntityNotFoundException("HelpRequest not found");
                }
            }
            case DONATION -> {
                if (!donationRepository.existsById(targetEntityId)) {
                    throw new EntityNotFoundException("Donation not found");
                }
            }
        }

        // Intenta reactivar una proposal expirada antes de crear una nueva
        int reactivated = proposalRepository.reactivateExpiredProposal(
                volunteerId, targetEntityId, type, score);

        if (reactivated > 0) {
            log.debug("[proposal] Reactivated expired proposal for volunteer {} entity {}",
                    volunteerId, targetEntityId);
            return proposalRepository
                    .findByTargetEntityIdAndVolunteer_Id(targetEntityId, volunteerId)
                    .map(proposalMapper::toDto)
                    .orElseThrow(() -> new RuntimeException("Proposal not found after reactivation"));
        }

        // crea una nueva proposal
        Proposal proposal = Proposal.builder()
                .volunteer(volunteer)
                .targetEntityId(targetEntityId)
                .type(type)
                .score(score)
                .status(ProposalStatus.PENDING)
                .build();

        Proposal saved = proposalRepository.save(proposal);
        return proposalMapper.toDto(saved);
    }

    /**
     * Obtiene proposals de un voluntario paginadas y mapeadas a DTOs.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProposalResponseDto> getProposalsByVolunteer(UUID volunteerId, int page, int size) {
        Volunteer volunteer = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new EntityNotFoundException("Volunteer not found"));

        Pageable pageable = PageRequest.of(page, size);
        return proposalRepository.findByVolunteer(volunteer, pageable)
                .map(proposalMapper::toDto);
    }

    /**
     * Obtiene proposals filtradas por estado y mapeadas a DTOs.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProposalResponseDto> getProposalsByStatus(ProposalStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return proposalRepository.findByStatus(status, pageable)
                .map(proposalMapper::toDto);
    }

    /**
     * Recupera una proposal específica por voluntario y entidad objetivo.
     */
    @Override
    @Transactional(readOnly = true)
    public ProposalResponseDto getProposal(UUID volunteerId, UUID targetEntityId) {
        Proposal proposal = proposalRepository.findByTargetEntityIdAndVolunteer_Id(targetEntityId, volunteerId)
                .orElseThrow(() -> new EntityNotFoundException("Proposal not found"));
        return proposalMapper.toDto(proposal);
    }

    @Override
    public ProposalResponseDto acceptProposal(UUID volunteerId, UUID proposalId) {

        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new EntityNotFoundException("Proposal not found"));

        if (!proposal.getVolunteer().getId().equals(volunteerId)) {
            throw new AccessDeniedException("Cannot accept another volunteer proposal");
        }

        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new IllegalStateException("Proposal already processed");
        }

        try {

            proposal.setStatus(ProposalStatus.ACCEPTED);
            proposal.setRespondedAt(LocalDateTime.now());

            Proposal saved = proposalRepository.save(proposal);

            assignTargetEntity(saved);

            cancelOtherProposals(saved.getTargetEntityId(), saved.getVolunteer().getId());

            return proposalMapper.toDto(saved);

        } catch (OptimisticLockException e) {

            throw new IllegalStateException("This proposal has already been processed by another volunteer");
        }
    }

    @Override
    public ProposalResponseDto rejectProposal(UUID volunteerId, UUID proposalId) {

        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new EntityNotFoundException("Proposal not found"));

        if (!proposal.getVolunteer().getId().equals(volunteerId)) {
            throw new AccessDeniedException("Cannot reject another volunteer proposal");
        }

        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new IllegalStateException("Proposal already processed");
        }

        proposal.setStatus(ProposalStatus.REJECTED);
        proposal.setRespondedAt(LocalDateTime.now());

        Proposal saved = proposalRepository.save(proposal);

        return proposalMapper.toDto(saved);
    }

    @Override
    public void cancelOtherProposals(UUID targetEntityId, UUID acceptedVolunteerId) {
        List<Proposal> proposals =
                proposalRepository.findPendingByTargetEntityExcludingVolunteer(targetEntityId, acceptedVolunteerId);

        for (Proposal proposal : proposals) {
            if (!proposal.getVolunteer().getId().equals(acceptedVolunteerId)) {
                proposal.setStatus(ProposalStatus.CANCELLED);
                proposal.setActive(false);
            }
        }

        proposalRepository.saveAll(proposals);
    }

    /**
     * Asigna la entidad objetivo (Donation o HelpRequest)
     * al voluntario que aceptó la proposal.
     */
    private void assignTargetEntity(Proposal proposal) {

        switch (proposal.getType()) {

            case HELP_REQUEST -> {

                HelpRequest helpRequest = helpRequestRepository
                        .findById(proposal.getTargetEntityId())
                        .orElseThrow(() -> new EntityNotFoundException("HelpRequest not found"));

                helpRequest.setStatus(HelpRequestStatus.ACCEPTED);
                helpRequest.setVolunteer(proposal.getVolunteer());
                helpRequest.setActive(false);

                helpRequestRepository.save(helpRequest);
            }

            case DONATION -> {

                Donation donation = donationRepository
                        .findById(proposal.getTargetEntityId())
                        .orElseThrow(() -> new EntityNotFoundException("Donation not found"));

                donation.setStatus(DonationStatus.RESERVED);
                donation.setVolunteer(proposal.getVolunteer());
                donation.setActive(false);

                donationRepository.save(donation);
            }
        }
    }

}
