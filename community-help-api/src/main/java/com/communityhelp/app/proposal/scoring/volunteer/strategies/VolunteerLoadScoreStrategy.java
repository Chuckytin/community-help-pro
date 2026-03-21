package com.communityhelp.app.proposal.scoring.volunteer.strategies;

import com.communityhelp.app.proposal.config.ProposalMatchingConfig;
import com.communityhelp.app.proposal.matching.MatchingContext;
import com.communityhelp.app.proposal.scoring.ScoreStrategy;
import com.communityhelp.app.volunteer.model.Volunteer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VolunteerLoadScoreStrategy<T> implements ScoreStrategy<T> {

    private final ProposalMatchingConfig proposalMatchingConfig;

    @Override
    public double calculate(
            T entity,
            Volunteer volunteer,
            MatchingContext context
    ) {

        long pending = context.pendingCounts().getOrDefault(volunteer.getId(), 0L);

        /*
         penalización suave:

         0 proposals: 1.0
         1 proposal: 0.5
         2 proposals: 0.33
         3 proposals: 0.25
         */

        return 1.0 / (1 + pending);
    }

    @Override
    public double weight(MatchingContext context) {
        return proposalMatchingConfig.getMaxWeightLoad();
    }
}
