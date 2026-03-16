package com.communityhelp.app.proposal.service;

import com.communityhelp.app.proposal.config.ProposalMatchingConfig;
import com.communityhelp.app.proposal.model.Proposal;
import com.communityhelp.app.proposal.repository.ProposalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Servicio encargado de mantener el ranking de proposals para cada voluntario.
 * - Ordena proposals por score.
 * - Limita el número máximo de propuestas activas.
 * - Mantiene relevancia del sistema.
 * Este componente evita la sobrecarga de propuestas y mejora la experiencia del voluntario.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProposalRankingService {

    private final ProposalRepository proposalRepository;

    /**
     * Recalcula el ranking de propuestas para un voluntario.
     * Mantiene únicamente las mejores propuestas activas según su score.
     */
    public void refreshRanking(UUID volunteerId) {

        List<Proposal> proposals =
                proposalRepository.findAllByVolunteer_IdOrderByScoreDesc(volunteerId);

        if (proposals.size() <= ProposalMatchingConfig.MAX_ACTIVE_PROPOSALS) {
            return;
        }

        List<Proposal> toDeactivate =
                proposals.subList(ProposalMatchingConfig.MAX_ACTIVE_PROPOSALS, proposals.size());

        toDeactivate.forEach(p -> p.setActive(false));
    }
}