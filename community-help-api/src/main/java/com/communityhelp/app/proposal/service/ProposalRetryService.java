package com.communityhelp.app.proposal.service;

import com.communityhelp.app.proposal.config.ProposalMatchingConfig;
import com.communityhelp.app.proposal.matching.model.ProposalMatchingState;
import com.communityhelp.app.proposal.matching.service.ProposalMatchingStateService;
import com.communityhelp.app.proposal.model.ProposalStatus;
import com.communityhelp.app.proposal.model.ProposalType;
import com.communityhelp.app.proposal.repository.ProposalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Servicio encargado de reintentar generar proposals
 * cuando las anteriores no han sido respondidas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProposalRetryService {

    private final ProposalRepository proposalRepository;
    private final ProposalGeneratorService generatorService;
    private final ProposalMatchingConfig proposalMatchingConfig;
    private final ProposalMatchingStateService matchingStateService;
    private final ProposalExpirationService expirationService;

    /**
     * Reintenta matching para entidades con proposals pendientes
     * que superaron el tiempo de espera cada 10 minutos.
     */
    @Scheduled(fixedDelayString = "${proposal.retry-delay.interval-ms}")
    public void retryUnansweredProposals() {

        LocalDateTime threshold = LocalDateTime.now()
                .minusMinutes(proposalMatchingConfig.getRetryDelayMinutes());

        log.debug("[proposal-retry] threshold={}", threshold);

        Set<UUID> helpRequests = proposalRepository.findDistinctTargetEntityIdsForRetry(
                ProposalType.HELP_REQUEST, ProposalStatus.PENDING, threshold);

        Set<UUID> donations = proposalRepository.findDistinctTargetEntityIdsForRetry(
                ProposalType.DONATION, ProposalStatus.PENDING, threshold);

        if (helpRequests.isEmpty() && donations.isEmpty()) {
            log.debug("[proposal-retry] Nothing to retry");
            return;
        }

        log.info("[proposal-retry] Retrying {} help requests and {} donations",
                helpRequests.size(), donations.size());

        expirationService.expireStaleProposals(threshold);

        helpRequests.forEach(id -> {
            ProposalMatchingState state = matchingStateService.getNextState(id, ProposalType.HELP_REQUEST);
            log.info("[proposal-retry] HelpRequest {} retry={} radius={}m",
                    id, state.getRetryCount(), state.getCurrentRadiusMeters());
            generatorService.generateForHelpRequest(
                    generatorService.getHelpRequestById(id),
                    state.getCurrentRadiusMeters(),
                    state.getRetryCount(),
                    true
            );
        });

        donations.forEach(id -> {
            ProposalMatchingState state = matchingStateService.getNextState(id, ProposalType.DONATION);
            log.info("[proposal-retry] Donation {} retry={} radius={}m",
                    id, state.getRetryCount(), state.getCurrentRadiusMeters());
            generatorService.generateForDonation(
                    generatorService.getDonationById(id),
                    state.getCurrentRadiusMeters(),
                    state.getRetryCount(),
                    true
            );
        });

        log.debug("[proposal-retry] Retry completed");
    }

}