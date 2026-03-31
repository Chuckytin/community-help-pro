package com.communityhelp.app.proposal.matching;

import com.communityhelp.app.proposal.config.ProposalMatchingConfig;
import com.communityhelp.app.proposal.scoring.ScoreStrategy;
import com.communityhelp.app.volunteer.dto.VolunteerCandidate;
import com.communityhelp.app.volunteer.model.Volunteer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Motor genérico de selección de candidatos.
 * Calcula el ranking de voluntarios según score y devuelve los mejores candidatos.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class MatchingEngine {

    private final ProposalMatchingConfig proposalMatchingConfig;

    public <T> List<Map.Entry<Volunteer, Double>> rankCandidates(
            T entity,
            List<VolunteerCandidate> candidates,
            ScoreStrategy<T> scoreStrategy,
            Map<UUID, Long> pendingCounts,
            int retryCount,
            Map<UUID, Double> travelTimes
    ) {
        int limit = proposalMatchingConfig.getMaxProposalsPerEntity();

        PriorityQueue<Map.Entry<Volunteer, Double>> topCandidates =
                new PriorityQueue<>(Map.Entry.comparingByValue());

        for (VolunteerCandidate candidate : candidates) {
            Volunteer volunteer = candidate.volunteer();
            double distance = candidate.distanceMeters();

            // Obtener tiempo de viaje real usando el modo de transporte del voluntario
            double travelSeconds = travelTimes.getOrDefault(volunteer.getId(), 0.0);

            // Calcular segundos disponibles (para deadline/expiryDate )
            long availableSeconds = Long.MAX_VALUE;

            MatchingContext context = new MatchingContext(
                    distance, travelSeconds, pendingCounts, retryCount, availableSeconds);

            double score = scoreStrategy.calculate(entity, volunteer, context);

            log.debug(
                    "[matching-score] volunteer {} | score {} | distance {}m | travel {}s",
                    volunteer.getId(),
                    String.format("%.3f", score),
                    Math.round(distance),
                    Math.round(travelSeconds)
            );

            if (score <= 0) continue;

            Map.Entry<Volunteer, Double> entry = Map.entry(volunteer, score);

            if (topCandidates.size() < limit) {
                topCandidates.add(entry);
            } else if (score > Objects.requireNonNull(topCandidates.peek()).getValue()) {
                topCandidates.poll();
                topCandidates.add(entry);
            }
        }

        return topCandidates.stream()
                .sorted(Map.Entry.<Volunteer, Double>comparingByValue().reversed())
                .toList();
    }


}