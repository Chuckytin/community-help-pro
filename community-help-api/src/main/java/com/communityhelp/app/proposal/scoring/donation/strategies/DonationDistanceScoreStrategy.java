package com.communityhelp.app.proposal.scoring.donation.strategies;

import com.communityhelp.app.donation.model.Donation;
import com.communityhelp.app.proposal.config.ProposalMatchingConfig;
import com.communityhelp.app.proposal.matching.MatchingContext;
import com.communityhelp.app.proposal.scoring.ScoreStrategy;
import com.communityhelp.app.volunteer.model.Volunteer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Strategy de scoring basada en distancia para donaciones.
 */
@Service
@RequiredArgsConstructor
public class DonationDistanceScoreStrategy implements ScoreStrategy<Donation> {

    private final ProposalMatchingConfig proposalMatchingConfig;

    /**
     * Calcula la puntuación de distancia entre donación y voluntario.
     */
    @Override
    public double calculate(
            Donation donation,
            Volunteer volunteer,
            MatchingContext context
    ) {

        double radiusMeters = volunteer.getRadiusKm() * 1000;
        double distanceMeters = context.distanceMeters();

        if (distanceMeters > radiusMeters) {
            return 0;
        }

        double normalized = 1 - (distanceMeters / radiusMeters);

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
