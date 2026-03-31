package com.communityhelp.app.proposal.scoring.helprequest.strategies;

import com.communityhelp.app.helprequest.model.HelpRequest;
import com.communityhelp.app.proposal.config.ProposalMatchingConfig;
import com.communityhelp.app.proposal.matching.MatchingContext;
import com.communityhelp.app.proposal.scoring.ScoreStrategy;
import com.communityhelp.app.volunteer.model.Volunteer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Strategy que calcula puntuación basada en proximidad geográfica entre voluntario y helprequest.
 */
@Service
@RequiredArgsConstructor
public class HelpRequestDistanceScoreStrategy implements ScoreStrategy<HelpRequest> {

    private final ProposalMatchingConfig proposalMatchingConfig;

    /**
     * Otorga mayor puntuación cuanto más cerca esté el voluntario del helprequest, normalizando por el radio de búsqueda del voluntario.
     */
    @Override
    public double calculate(HelpRequest helpRequest, Volunteer volunteer, MatchingContext context) {

        double radiusMeters = volunteer.getRadiusKm() * 1000;
        double distanceMeters = context.distanceMeters();

        if (distanceMeters > radiusMeters) return 0;

        // Si tenemos tiempo real de viaje, lo usamos para normalizar
        // Si no (travelSeconds = 0), fallback a distancia en metros
        double travelSeconds = context.travelSeconds();
        double normalized;

        if (travelSeconds > 0) {
            // Normaliza contra 30 minutos (1800s) como tiempo máximo razonable
            double maxReasonableSeconds = context.availableSeconds();
            normalized = Math.max(0, 1 - (travelSeconds / maxReasonableSeconds));
        } else {
            normalized = 1 - (distanceMeters / radiusMeters);
        }

        return normalized * proposalMatchingConfig.getMaxDistanceScore();
    }

    @Override
    public double weight(MatchingContext context) {
        return Math.max(
                proposalMatchingConfig.getMaxWeightDistance() - (context.retryCount() * 0.05),
                0.20
        );
    }
}
