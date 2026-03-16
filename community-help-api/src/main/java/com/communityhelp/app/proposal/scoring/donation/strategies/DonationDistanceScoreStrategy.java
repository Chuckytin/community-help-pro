package com.communityhelp.app.proposal.scoring.donation.strategies;

import com.communityhelp.app.donation.model.Donation;
import com.communityhelp.app.proposal.config.ProposalMatchingConfig;
import com.communityhelp.app.proposal.matching.MatchingContext;
import com.communityhelp.app.proposal.scoring.ScoreStrategy;
import com.communityhelp.app.volunteer.model.Volunteer;
import org.springframework.stereotype.Service;

/**
 * Strategy de scoring basada en distancia para donaciones.
 */
@Service
public class DonationDistanceScoreStrategy implements ScoreStrategy<Donation> {

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

        return normalized * ProposalMatchingConfig.MAX_DISTANCE_SCORE;
    }

    @Override
    public double weight() {
        return ProposalMatchingConfig.MAX_WEIGHT_DISTANCE;
    }

}
