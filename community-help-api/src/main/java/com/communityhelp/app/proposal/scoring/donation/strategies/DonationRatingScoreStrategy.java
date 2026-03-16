package com.communityhelp.app.proposal.scoring.donation.strategies;

import com.communityhelp.app.donation.model.Donation;
import com.communityhelp.app.proposal.config.ProposalMatchingConfig;
import com.communityhelp.app.proposal.matching.MatchingContext;
import com.communityhelp.app.proposal.scoring.ScoreStrategy;
import com.communityhelp.app.volunteer.model.Volunteer;
import org.springframework.stereotype.Service;

@Service
public class DonationRatingScoreStrategy implements ScoreStrategy<Donation> {

    @Override
    public double calculate(
            Donation donation,
            Volunteer volunteer,
            MatchingContext context
    ) {

        if (volunteer.getUser().getRating() == null) {
            return 0;
        }

        return volunteer.getUser().getRating() / 5.0;
    }

    @Override
    public double weight() {
        return ProposalMatchingConfig.MAX_WEIGHT_RATING;
    }
}
