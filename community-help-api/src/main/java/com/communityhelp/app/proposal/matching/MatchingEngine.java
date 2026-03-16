package com.communityhelp.app.proposal.matching;

import com.communityhelp.app.proposal.config.ProposalMatchingConfig;
import com.communityhelp.app.proposal.scoring.ScoreStrategy;
import com.communityhelp.app.volunteer.dto.VolunteerCandidate;
import com.communityhelp.app.volunteer.model.Volunteer;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;

/**
 * Motor genérico de selección de candidatos.
 * Calcula el ranking de voluntarios según score y devuelve los mejores candidatos.
 */
@Slf4j
public class MatchingEngine {

    public static <T> List<Map.Entry<Volunteer, Double>> rankCandidates(
            T entity,
            List<VolunteerCandidate> candidates,
            ScoreStrategy<T> scoreStrategy,
            Map<UUID, Long> pendingCounts
    ) {

        int limit = ProposalMatchingConfig.MAX_PROPOSALS_PER_ENTITY;

        PriorityQueue<Map.Entry<Volunteer, Double>> topCandidates =
                new PriorityQueue<>(Map.Entry.comparingByValue());

        for (VolunteerCandidate candidate : candidates) {

            Volunteer volunteer = candidate.volunteer();
            double distance = candidate.distanceMeters();

            MatchingContext context = new MatchingContext(distance, pendingCounts);

            double score = scoreStrategy.calculate(entity, volunteer, context);

            log.debug(
                    "[matching-score] volunteer {} | score {} | distance {}m",
                    volunteer.getId(),
                    String.format("%.3f", score),
                    Math.round(distance)
            );

            if (score <= 0) continue;

            Map.Entry<Volunteer, Double> entry = Map.entry(volunteer, score);

            if (topCandidates.size() < limit) {
                topCandidates.add(entry);
            } else if (score > topCandidates.peek().getValue()) {
                topCandidates.poll();
                topCandidates.add(entry);
            }
        }

        return topCandidates.stream()
                .sorted(Map.Entry.<Volunteer, Double>comparingByValue().reversed())
                .toList();
    }
}