package com.communityhelp.app.proposal.scoring.helprequest.strategies;

import com.communityhelp.app.helprequest.model.HelpRequest;
import com.communityhelp.app.proposal.config.ProposalMatchingConfig;
import com.communityhelp.app.proposal.matching.MatchingContext;
import com.communityhelp.app.proposal.scoring.ScoreStrategy;
import com.communityhelp.app.volunteer.model.Volunteer;
import org.springframework.stereotype.Service;

/**
 * Strategy que calcula puntuación basada en proximidad geográfica entre voluntario y helprequest.
 */
@Service
public class HelpRequestDistanceScoreStrategy implements ScoreStrategy<HelpRequest> {

    /**
     * Otorga mayor puntuación cuanto menor sea la distancia.
     */
    @Override
    public double calculate(
            HelpRequest helpRequest,
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
