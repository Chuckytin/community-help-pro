package com.communityhelp.app.proposal.scoring.donation;

import com.communityhelp.app.donation.model.Donation;
import com.communityhelp.app.proposal.matching.MatchingContext;
import com.communityhelp.app.proposal.scoring.ScoreStrategy;
import com.communityhelp.app.volunteer.model.Volunteer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Motor de cálculo de score para Donation.
 * Agrega todas las ScoreStrategy registradas y combina sus resultados para una puntuación final.
 */
@Service
@RequiredArgsConstructor
public class DonationScoreEngine implements ScoreStrategy<Donation> {

    private final List<ScoreStrategy<Donation>> strategies;

    /**
     * Calcula el score total sumando todas las estrategias activas.
     */
    @Override
    public double calculate(Donation donation, Volunteer volunteer, MatchingContext context) {
        return strategies.stream()
                .mapToDouble(s -> s.calculate(donation, volunteer, context) * s.weight())
                .sum();
    }

    @Override
    public double weight() {
        return 1.0;
    }
}
