package com.communityhelp.app.proposal.scoring;

import com.communityhelp.app.proposal.matching.MatchingContext;
import com.communityhelp.app.volunteer.model.Volunteer;

/**
 * Utilizado por el MatchingEngine para calcular el score entre una entidad y un voluntario.
 */
public interface ScoreStrategy<T> {

    double calculate(T target, Volunteer volunteer, MatchingContext context);

    double weight(MatchingContext context);
}