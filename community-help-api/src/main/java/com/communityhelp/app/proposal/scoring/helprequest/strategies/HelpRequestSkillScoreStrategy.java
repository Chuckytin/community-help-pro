package com.communityhelp.app.proposal.scoring.helprequest.strategies;

import com.communityhelp.app.helprequest.model.HelpRequest;
import com.communityhelp.app.proposal.config.ProposalMatchingConfig;
import com.communityhelp.app.proposal.matching.MatchingContext;
import com.communityhelp.app.proposal.scoring.ScoreStrategy;
import com.communityhelp.app.proposal.util.SkillMatcher;
import com.communityhelp.app.volunteer.model.Volunteer;
import com.communityhelp.app.volunteer.model.VolunteerSkill;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Strategy que calcula el score según coincidencia de habilidades
 * entre el voluntario y las requeridas por la solicitud.
 */
@Service
public class HelpRequestSkillScoreStrategy implements ScoreStrategy<HelpRequest> {

    /**
     * Cuenta cuántas skills del voluntario coinciden con las necesarias.
     */
    @Override
    public double calculate(HelpRequest helpRequest, Volunteer volunteer, MatchingContext context) {

        Set<VolunteerSkill> requiredSkills =
                SkillMatcher.HELP_REQUEST_SKILLS
                        .getOrDefault(helpRequest.getType(), Set.of());

        return requiredSkills.stream()
                .filter(volunteer.getSkills()::contains)
                .count();
    }

    @Override
    public double weight() {
        return ProposalMatchingConfig.MAX_WEIGHT_SKILLS;
    }
}
