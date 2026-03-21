package com.communityhelp.app.proposal.listener;

import com.communityhelp.app.proposal.matching.service.ProposalMatchingStateService;
import com.communityhelp.app.proposal.model.ProposalType;
import com.communityhelp.app.volunteer.service.VolunteerReevaluationService;
import com.communityhelp.app.donation.event.DonationCreatedEvent;
import com.communityhelp.app.donation.event.DonationUpdatedEvent;
import com.communityhelp.app.helprequest.event.HelpRequestCreatedEvent;
import com.communityhelp.app.helprequest.event.HelpRequestUpdatedEvent;
import com.communityhelp.app.volunteer.event.VolunteerUpdatedEvent;
import com.communityhelp.app.proposal.service.ProposalGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

/**
 * Listener encargado de reaccionar a eventos del dominio para generar automáticamente proposals.
 * Eventos manejados:
 * - HelpRequestCreatedEvent
 * - HelpRequestUpdatedEvent
 * - DonationCreatedEvent
 * - VolunteerUpdatedEvent
 * Permite que el sistema sea reactivo sin acoplar servicios entre sí.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProposalGenerationListener {

    private final ProposalGeneratorService proposalGeneratorService;
    private final VolunteerReevaluationService volunteerReevaluationService;
    private final ProposalMatchingStateService matchingStateService;

    /**
     * Genera proposals tras la creación de una HelpRequest.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onHelpRequestCreated(HelpRequestCreatedEvent event) {
        matchingStateService.initState(event.helpRequestId(), ProposalType.HELP_REQUEST);
        handleHelpRequestMatching(event.helpRequestId(), "new");
    }

    /**
     * Regenera proposals cuando una HelpRequest se actualiza.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onHelpRequestUpdated(HelpRequestUpdatedEvent event) {
        handleHelpRequestMatching(event.helpRequestId(), "updated");
    }

    /**
     * Genera proposals tras la creación de una Donation.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDonationCreated(DonationCreatedEvent event) {
        matchingStateService.initState(event.donationId(), ProposalType.DONATION);
        handleDonationMatching(event.donationId(), "new");
    }

    /**
     * Regenera proposals cuando una Donation se actualiza.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDonationUpdated(DonationUpdatedEvent event) {
        handleDonationMatching(event.donationId(), "updated");
    }

    /**
     * Reevalúa proposals cuando cambia un voluntario.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVolunteerUpdated(VolunteerUpdatedEvent event) {
        log.info("[event-volunteer] Volunteer updated {}, reevaluating proposals", event.volunteerId());
        volunteerReevaluationService.reevaluate(event.volunteerId());
    }

    /**
     * Ejecuta matching para una HelpRequest.
     */
    private void handleHelpRequestMatching(UUID helpRequestId, String action) {
        log.info("[event-helprequest] Generating proposals for {} HelpRequest {}", action, helpRequestId);
        proposalGeneratorService.generateForHelpRequest(
                proposalGeneratorService.getHelpRequestById(helpRequestId)
        );
    }

    /**
     * Ejecuta matching para una Donation.
     */
    private void handleDonationMatching(UUID donationId, String action) {
        log.info("[event-donation] Generating proposals for {} Donation {}", action, donationId);
        proposalGeneratorService.generateForDonation(
                proposalGeneratorService.getDonationById(donationId)
        );
    }
}