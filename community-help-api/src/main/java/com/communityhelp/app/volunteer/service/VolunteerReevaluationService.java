package com.communityhelp.app.volunteer.service;

import com.communityhelp.app.donation.model.Donation;
import com.communityhelp.app.donation.repository.DonationRepository;
import com.communityhelp.app.helprequest.model.HelpRequest;
import com.communityhelp.app.helprequest.repository.HelpRequestRepository;
import com.communityhelp.app.proposal.service.ProposalGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Servicio encargado de reevaluar proposals cuando un Volunteer cambia.
 * Evita recalcular el sistema realizando una regeneración
 * selectiva basada en entidades activas.
 * - Busca HelpRequests activas
 * - Busca Donations activas
 * - Recalcula matching para el voluntario actualizado
 * Este servicio prepara el sistema para futura ejecución async
 * (colas, workers, event streaming).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VolunteerReevaluationService {

    private final HelpRequestRepository helpRequestRepository;
    private final DonationRepository donationRepository;
    private final ProposalGeneratorService proposalGeneratorService;

    /**
     * Reevalúa proposals afectadas por cambios en un voluntario.
     */
    @Transactional
    public void reevaluate(UUID volunteerId) {

        log.info("[reevaluate-volunteer] Reevaluating proposals for volunteer {}", volunteerId);

        List<HelpRequest> activeHelpRequests = helpRequestRepository.findAllActive();

        for (HelpRequest helpRequest : activeHelpRequests) {
            proposalGeneratorService.generateForHelpRequest(helpRequest);
        }

        List<Donation> activeDonations = donationRepository.findAllActive();

        for (Donation donation : activeDonations) {
            proposalGeneratorService.generateForDonation(donation);
        }
    }
}