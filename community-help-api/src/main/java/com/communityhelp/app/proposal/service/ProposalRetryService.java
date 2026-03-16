package com.communityhelp.app.proposal.service;

import com.communityhelp.app.proposal.config.ProposalMatchingConfig;
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

    /**
     * Reintenta matching para entidades con proposals pendientes
     * que superaron el tiempo de espera.
     */
    @Scheduled(fixedDelay = 600000) // cada 10 minutos
    public void retryUnansweredProposals() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusMinutes(ProposalMatchingConfig.RETRY_DELAY_MINUTES);

        log.debug(
                "[proposal-retry] Ejecutando reintento - threshold={} (hace {} minutos), now={}",
                threshold,
                ProposalMatchingConfig.RETRY_DELAY_MINUTES,
                now
        );

        Set<UUID> helpRequests =
                proposalRepository.findDistinctTargetEntityIdsForRetry(
                        ProposalType.HELP_REQUEST,
                        ProposalStatus.PENDING,
                        threshold
                );

        Set<UUID> donations =
                proposalRepository.findDistinctTargetEntityIdsForRetry(
                        ProposalType.DONATION,
                        ProposalStatus.PENDING,
                        threshold
                );

        if (helpRequests.isEmpty() && donations.isEmpty()) {
            log.debug("[proposal-retry] No pending proposals to retry");
            return;
        }

        log.info(
                "[proposal-retry] Retrying {} help requests and {} donations",
                helpRequests.size(),
                donations.size()
        );

        if (log.isDebugEnabled()) {
            log.debug("[proposal-retry] HelpRequest count: {}", helpRequests.size());
            log.debug("[proposal-retry] Donation count: {}", donations.size());
        }

        helpRequests.forEach(id ->
                generatorService.generateForHelpRequest(
                        generatorService.getHelpRequestById(id)
                )
        );

        donations.forEach(id ->
                generatorService.generateForDonation(
                        generatorService.getDonationById(id)
                )
        );

        log.debug("[proposal-retry] Reintento completado");
    }
}