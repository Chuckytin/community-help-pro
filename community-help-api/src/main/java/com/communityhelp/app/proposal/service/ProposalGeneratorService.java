package com.communityhelp.app.proposal.service;

import com.communityhelp.app.donation.model.Donation;
import com.communityhelp.app.donation.repository.DonationRepository;
import com.communityhelp.app.helprequest.model.HelpRequest;
import com.communityhelp.app.helprequest.repository.HelpRequestRepository;
import com.communityhelp.app.proposal.config.ProposalMatchingConfig;
import com.communityhelp.app.proposal.matching.MatchingEngine;
import com.communityhelp.app.proposal.model.ProposalStatus;
import com.communityhelp.app.proposal.model.ProposalType;
import com.communityhelp.app.proposal.repository.ProposalRepository;
import com.communityhelp.app.proposal.scoring.donation.DonationScoreEngine;
import com.communityhelp.app.proposal.scoring.helprequest.HelpRequestScoreEngine;
import com.communityhelp.app.volunteer.dto.VolunteerCandidate;
import com.communityhelp.app.volunteer.model.Volunteer;
import com.communityhelp.app.volunteer.repository.VolunteerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio encargado de generar proposals automáticas.
 * Conecta a voluntarios con HelpRequests y Donations.
 * - Evalua candidatos mediante ScoreEngines.
 * - Evita duplicados.
 * - Crea propuestas únicamente si el score es relevante.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProposalGeneratorService {

    private final ProposalRepository proposalRepository;
    private final VolunteerRepository volunteerRepository;
    private final HelpRequestRepository helpRequestRepository;
    private final DonationRepository donationRepository;
    private final ProposalService proposalService;
    private final HelpRequestScoreEngine helpRequestScoreEngine;
    private final DonationScoreEngine  donationScoreEngine;
    private final ProposalRankingService rankingService;
    private final ProposalMatchingConfig proposalMatchingConfig;
    private final MatchingEngine matchingEngine;

    /**
     * Genera proposals para los mejores voluntarios disponibles para una HelpRequest.
     * - Obtiene voluntarios disponibles dentro del radio máximo configurado.
     * - Excluye al usuario que creó la solicitud
     * - Calcula el score de cada voluntario mediante HelpRequestScoreEngine.
     * - Ordena los voluntarios por score descendente.
     * - Selecciona los mejores candidatos, limitado por MAX_PROPOSALS_PER_ENTITY
     * - Filtra voluntarios que:
     * - - Ya alcanzaron el máximo de proposals activas
     * - - están en periodo de cooldown
     * - - ya tienen una proposal para esta entidad
     * Es el núcleo del motor de matching entre HelpRequest y voluntarios disponibles.
     */
    public void generateForHelpRequest(HelpRequest helpRequest, int radiusMeters, int retryCount) {

        long start = System.currentTimeMillis();

        if (!helpRequest.isActive()) return;

        List<Object[]> rows =
                volunteerRepository.findNearbyVolunteerIds(
                        helpRequest.getLocation(),
                        radiusMeters,
                        helpRequest.getRequester().getId(),
                        PageRequest.of(
                                0,
                                proposalMatchingConfig.getMaxProposalsPerEntity() *
                                        proposalMatchingConfig.getMaxCandidatesMultiplier()
                        )
                );

        long afterFetch = System.currentTimeMillis();

        if (rows.isEmpty()) {
            log.warn("[matching-helprequest] No volunteers available for HelpRequest {}",
                    helpRequest.getId());
            return;
        }

        Map<UUID, Double> distanceMap =
                rows.stream()
                        .collect(Collectors.toMap(
                                r -> (UUID) r[0],
                                r -> ((Number) r[1]).doubleValue(),
                                (a, b) -> a
                        ));

        List<Volunteer> volunteers = volunteerRepository.findAllByIdWithUser(distanceMap.keySet());

        List<VolunteerCandidate> candidates =
                volunteers.stream()
                        .map(v -> new VolunteerCandidate(
                                v,
                                distanceMap.get(v.getId())
                        ))
                        .toList();

        log.info("[matching-helprequest] Volunteers found for HelpRequest {}: {}",
                helpRequest.getId(),
                candidates.size());

        Map<UUID, Long> pendingCounts = loadPendingCounts(volunteers);
        Map<UUID, LocalDateTime> lastResponses = loadLastResponses(volunteers);

        long afterPreload = System.currentTimeMillis();

        List<Map.Entry<Volunteer, Double>> ranked =
                matchingEngine.rankCandidates(
                        helpRequest,
                        candidates,
                        helpRequestScoreEngine,
                        pendingCounts,
                        retryCount
                );

        long afterRanking = System.currentTimeMillis();

        Set<UUID> existingProposals = new HashSet<>(
                proposalRepository.findVolunteerIdsWithProposal(helpRequest.getId()));

        int created = createProposals(
                helpRequest.getId(),
                ranked,
                ProposalType.HELP_REQUEST,
                pendingCounts,
                lastResponses,
                existingProposals
        );

        long end = System.currentTimeMillis();

        log.debug(
                "[matching-helprequest] Matching result for HelpRequest {} -> volunteers evaluated: {}, proposals created: {}, took {} ms",
                helpRequest.getId(),
                candidates.size(),
                created,
                System.currentTimeMillis() - start
        );

        log.debug(
                "[matching-timing] fetch={}ms preload={}ms ranking={}ms create={}ms total={}ms",
                (afterFetch - start),
                (afterPreload - afterFetch),
                (afterRanking - afterPreload),
                (end - afterRanking),
                (end - start)
        );
    }

    /**
     * Overload para el primer disparo desde el listener
     */
    public void generateForHelpRequest(HelpRequest helpRequest) {
        generateForHelpRequest(helpRequest, proposalMatchingConfig.getMaxRadiusDistance(), 0);
    }

    /**
     * Genera proposals para los mejores voluntarios disponibles para una Donation.
     * - Obtiene voluntarios disponibles dentro del radio máximo configurado.
     * - Excluye al usuario que creó la solicitud
     * - Calcula el score de cada voluntario mediante DonationScoreEngine.
     * - Ordena los voluntarios por score descendente.
     * - Selecciona los mejores candidatos, limitado por MAX_PROPOSALS_PER_ENTITY
     * - Filtra voluntarios que:
     * - - Ya alcanzaron el máximo de proposals activas
     * - - están en periodo de cooldown
     * - - ya tienen una proposal para esta entidad
     * Es el núcleo del motor de matching entre Donation y voluntarios disponibles.
     */
    public void generateForDonation(Donation donation, int radiusMeters, int retryCount) {

        long start = System.currentTimeMillis();

        if (!donation.isActive()) return;

        List<Object[]> rows =
                volunteerRepository.findNearbyVolunteerIds(
                        donation.getLocation(),
                        radiusMeters,
                        donation.getDonor().getId(),
                        PageRequest.of(
                                0,
                                proposalMatchingConfig.getMaxProposalsPerEntity() *
                                        proposalMatchingConfig.getMaxCandidatesMultiplier()
                        )
                );

        long afterFetch = System.currentTimeMillis();

        if (rows.isEmpty()) {
            log.warn("[matching-donation] No volunteers available for Donation {}",
                    donation.getId());
            return;
        }

        Map<UUID, Double> distanceMap =
                rows.stream()
                        .collect(Collectors.toMap(
                                r -> (UUID) r[0],
                                r -> ((Number) r[1]).doubleValue(),
                                (a, b) -> a
                        ));

        List<Volunteer> volunteers = volunteerRepository.findAllByIdWithUser(distanceMap.keySet());

        List<VolunteerCandidate> candidates =
                volunteers.stream()
                        .map(v -> new VolunteerCandidate(
                                v,
                                distanceMap.get(v.getId())
                        ))
                        .toList();

        log.info("[matching-donation] Volunteers found for Donation {}: {}",
                donation.getId(),
                candidates.size());

        Map<UUID, Long> pendingCounts = loadPendingCounts(volunteers);
        Map<UUID, LocalDateTime> lastResponses = loadLastResponses(volunteers);

        long afterPreload = System.currentTimeMillis();

        List<Map.Entry<Volunteer, Double>> ranked =
                matchingEngine.rankCandidates(
                        donation,
                        candidates,
                        donationScoreEngine,
                        pendingCounts,
                        retryCount
                );

        long afterRanking = System.currentTimeMillis();

        Set<UUID> existingProposals = new HashSet<>(
                proposalRepository.findVolunteerIdsWithProposal(donation.getId()));

        int created = createProposals(
                donation.getId(),
                ranked,
                ProposalType.DONATION,
                pendingCounts,
                lastResponses,
                existingProposals
        );

        long end = System.currentTimeMillis();

        log.debug(
                "[matching-donation] Matching result for Donation {} -> volunteers evaluated: {}, proposals created: {}, took {} ms",
                donation.getId(),
                candidates.size(),
                created,
                System.currentTimeMillis() - start
        );

        log.debug(
                "[matching-timing] fetch={}ms preload={}ms ranking={}ms create={}ms total={}ms",
                (afterFetch - start),
                (afterPreload - afterFetch),
                (afterRanking - afterPreload),
                (end - afterRanking),
                (end - start)
        );
    }

    /**
     * Overload para el primer disparo desde el listener
     */
    public void generateForDonation(Donation donation) {
        generateForDonation(donation, proposalMatchingConfig.getMaxRadiusDistance(), 0);
    }

    /**
     * Obtiene una HelpRequest por id o lanza excepción si no existe.
     */
    public HelpRequest getHelpRequestById(UUID helpRequestId) {
        return helpRequestRepository.findById(helpRequestId)
                .orElseThrow(() -> new EntityNotFoundException("HelpRequest not found"));
    }

    /**
     * Obtiene una Donation por id o lanza excepción si no existe.
     */
    public Donation getDonationById(UUID donationId) {
        return donationRepository.findById(donationId)
                .orElseThrow(() -> new EntityNotFoundException("Donation not found"));
    }

    /**
     * Crea proposals para los mejores voluntarios candidatos a partir de una lista de voluntarios.
     * Procesa la lista de voluntarios ordenados por puntuación y aplica los siguientes filtros:
     * - Máximo de proposals activas: Verifica que el voluntario no haya alcanzado el límite
     * - Período de cooldown: Verifica que el voluntario no haya respondido recientemente.
     * - Duplicados: Verifica que no exista ya una proposal para esta entidad.
     * Los voluntarios que pasan todos los filtros reciben una nueva proposal y se programa la actualización de su ranking.
     */
    private int createProposals(
            UUID entityId,
            List<Map.Entry<Volunteer, Double>> ranked,
            ProposalType type,
            Map<UUID, Long> pendingCounts,
            Map<UUID, LocalDateTime> lastResponses,
            Set<UUID> existingProposals
    ) {

        Set<UUID> affectedVolunteers = new HashSet<>();

        int created = 0;

        for (Map.Entry<Volunteer, Double> entry : ranked) {

            Volunteer volunteer = entry.getKey();
            double score = entry.getValue();

            if (!volunteer.isAvailable()) {
                log.debug("Skipping volunteer {} NOT_AVAILABLE", volunteer.getId());
                continue;
            }

            if (hasReachedMaxProposals(volunteer.getId(), pendingCounts)) {
                log.debug("Skipping volunteer {} MAX_PROPOSALS", volunteer.getId());
                continue;
            }

            if (isInCooldown(volunteer.getId(), lastResponses)) {
                log.debug("Skipping volunteer {} COOLDOWN", volunteer.getId());
                continue;
            }

            if (existingProposals.contains(volunteer.getId())) {
                log.debug("Skipping volunteer {} ALREADY_HAS_PROPOSAL", volunteer.getId());
                continue;
            }

            proposalService.createProposal(
                    volunteer.getId(),
                    entityId,
                    type,
                    score
            );

            affectedVolunteers.add(volunteer.getId());
            created++;

            log.debug(
                    "[proposal-created] Proposal created for volunteer {} score {}",
                    volunteer.getId(),
                    String.format("%.3f", score)
            );
        }

        affectedVolunteers.forEach(rankingService::refreshRanking);

        return created;
    }

    /**
     * Verifica si el voluntario ha alcanzado el máximo de proposals activas permitidas.
     * - Utiliza un mapa precargado de conteos para optimizar la verificación cuando se evalúan múltiples voluntarios.
     */
    private boolean hasReachedMaxProposals(UUID volunteerId, Map<UUID, Long> counts) {

        long activeProposals = counts.getOrDefault(volunteerId, 0L);
        return activeProposals >= proposalMatchingConfig.getMaxActiveProposals();
    }

    /**
     * Verifica si el voluntario está dentro del periodo de cooldown
     * tras haber respondido recientemente a una proposal.
     * - Utiliza un mapara precargado de últimas respuestas para optimizar la verificación
     * cuando se evalúan múltiples voluntarios.
     */
    private boolean isInCooldown(UUID volunteerId, Map<UUID, LocalDateTime> lastResponses) {

        LocalDateTime respondedAt = lastResponses.get(volunteerId);

        if (respondedAt == null) return false;

        return respondedAt
                .plusMinutes(proposalMatchingConfig.getProposalCooldownMinutes())
                .isAfter(LocalDateTime.now());
    }

    /**
     * Carga el conteo de proposals pendientes para una lista de voluntarios.
     * - Se utiliza durante el proceso de generación de proposals para filtrar rápidamente
     * aquellos voluntarios que yan han alcanzado el límite máximo de proposals activas.
     */
    private Map<UUID, Long> loadPendingCounts(List<Volunteer> volunteers) {

        List<UUID> volunteerIds =
                volunteers.stream()
                        .map(Volunteer::getId)
                        .toList();

        return proposalRepository
                .countByVolunteerIdsAndStatus(volunteerIds, ProposalStatus.PENDING)
                .stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]
                ));
    }

    /**
     * Carga la fecha de la última respuesta para una lista de voluntarios.
     * - Se utiliza durante el proceso de generación de proposals para verificar el período de cooldown
     * de múltiples voluntarios de forma eficiente.
     */
    private Map<UUID, LocalDateTime> loadLastResponses(List<Volunteer> volunteers) {

        List<UUID> volunteerIds =
                volunteers.stream()
                        .map(Volunteer::getId)
                        .toList();

        return proposalRepository
                .findLastResponsesByVolunteerIds(volunteerIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (LocalDateTime) row[1]
                ));
    }

}