package com.communityhelp.app.proposal.scoring.donation.strategies;

import com.communityhelp.app.donation.model.Donation;
import com.communityhelp.app.proposal.config.ProposalMatchingConfig;
import com.communityhelp.app.proposal.matching.MatchingContext;
import com.communityhelp.app.proposal.scoring.ScoreStrategy;
import com.communityhelp.app.volunteer.model.Volunteer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DonationRatingScoreStrategy implements ScoreStrategy<Donation> {

    private final ProposalMatchingConfig proposalMatchingConfig;

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
    public double weight(MatchingContext context) {
        return proposalMatchingConfig.getMaxWeightRating();
    }
}
