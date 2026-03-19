package com.communityhelp.app.proposal.service;

import com.communityhelp.app.proposal.config.ProposalMatchingConfig;
import com.communityhelp.app.proposal.model.ProposalStatus;
import com.communityhelp.app.proposal.model.ProposalType;
import com.communityhelp.app.proposal.repository.ProposalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * que superaron el tiempo de espera cada 10 minutos.
     */
    @Scheduled(fixedDelay = 600000)
    @Transactional
    public void retryUnansweredProposals() {

        LocalDateTime threshold = LocalDateTime.now()
                .minusMinutes(ProposalMatchingConfig.RETRY_DELAY_MINUTES);

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

        // sigue viendo las PENDING antiguas y bloquea la creación de nuevas
        proposalRepository.expireStaleProposals(ProposalType.HELP_REQUEST, threshold);
        proposalRepository.expireStaleProposals(ProposalType.DONATION, threshold);

        helpRequests.forEach(id ->
                generatorService.generateForHelpRequest(
                        generatorService.getHelpRequestById(id)));

        donations.forEach(id ->
                generatorService.generateForDonation(
                        generatorService.getDonationById(id)));

        log.debug("[proposal-retry] Retry completed");
    }
}