package com.communityhelp.app.proposal.scoring.helprequest;

import com.communityhelp.app.helprequest.model.HelpRequest;
import com.communityhelp.app.proposal.matching.MatchingContext;
import com.communityhelp.app.proposal.scoring.ScoreStrategy;
import com.communityhelp.app.volunteer.model.Volunteer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Motor de scoring para HelpRequest.
 * Ejecuta todas las estrategias registradas y combina
 * sus resultados para generar la puntuación final.
 */
@Service
@RequiredArgsConstructor
public class HelpRequestScoreEngine implements ScoreStrategy<HelpRequest> {

    private final List<ScoreStrategy<HelpRequest>> strategies;

    /**
     * Calcula el score total acumulando todas las strategies activas.
     */
    @Override
    public double calculate(HelpRequest helpRequest, Volunteer volunteer, MatchingContext context) {

        return strategies.stream()
                .mapToDouble(s -> s.calculate(helpRequest, volunteer, context) * s.weight())
                .sum();
    }

    @Override
    public double weight() {
        return 1.0;
    }

}
