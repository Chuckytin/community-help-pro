package com.communityhelp.app.proposal.scoring.helprequest.strategies;

import com.communityhelp.app.helprequest.model.HelpRequest;
import com.communityhelp.app.proposal.config.ProposalMatchingConfig;
import com.communityhelp.app.proposal.matching.MatchingContext;
import com.communityhelp.app.proposal.scoring.ScoreStrategy;
import com.communityhelp.app.volunteer.model.Volunteer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Strategy que calcula puntuación basada en el rating.
 */
@Service
@RequiredArgsConstructor
public class HelpRequestRatingScoreStrategy implements ScoreStrategy<HelpRequest> {

    private final ProposalMatchingConfig proposalMatchingConfig;

    /**
     * Otorga mayor puntuación cuanto mayor sea el rating (0-5).
     */
    @Override
    public double calculate(
            HelpRequest helpRequest,
            Volunteer volunteer,
            MatchingContext context
    ) {

        if (volunteer.getUser().getRating() == null) {
            return 0;
        }

        return volunteer.getUser().getRating() / 5.0;
    }

    @Override
    public double weight(MatchingContext context) {
        return proposalMatchingConfig.getMaxWeightRating();
    }
}
