package com.communityhelp.app.proposal.service;

import com.communityhelp.app.proposal.model.ProposalCancelReason;
import com.communityhelp.app.proposal.model.ProposalType;
import com.communityhelp.app.proposal.repository.ProposalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Componente auxiliar para operaciones de expiración que requieren
 * su propia transacción, separado de ProposalRetryService para
 * que Spring AOP pueda interceptar correctamente el @Transactional.
 */
@Service
@RequiredArgsConstructor
public class ProposalExpirationService {

    private final ProposalRepository proposalRepository;

    @Transactional
    public void expireStaleProposals(LocalDateTime threshold) {
        proposalRepository.expireStaleProposals(ProposalType.HELP_REQUEST, threshold, ProposalCancelReason.SYSTEM_EXPIRED);
        proposalRepository.expireStaleProposals(ProposalType.DONATION, threshold, ProposalCancelReason.SYSTEM_EXPIRED);
    }
}
