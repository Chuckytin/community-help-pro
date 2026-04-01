package com.communityhelp.app.proposal.service;

import com.communityhelp.app.common.openroute.model.TransportMode;
import com.communityhelp.app.common.openroute.service.TravelFeasibilityService;
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
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
    private final DonationScoreEngine donationScoreEngine;
    private final ProposalRankingService rankingService;
    private final ProposalMatchingConfig proposalMatchingConfig;
    private final MatchingEngine matchingEngine;
    private final TravelFeasibilityService travelFeasibilityService;

    /**
     * Genera proposals para los mejores voluntarios disponibles para una HelpRequest.
     * El proceso de matching sigue estos pasos:
     * 1. Obtiene voluntarios disponibles dentro del radio máximo configurado,
     * excluyendo al usuario que creó la solicitud.
     * 2. Calcula el tiempo de viaje estimado de cada voluntario al punto de la ayuda
     * (utilizando el medio de transporte del voluntario o FOOT_WALKING por defecto).
     * 3. Si la HelpRequest tiene fecha límite (deadline), filtra los voluntarios que pueden llegar a tiempo
     * utilizando TravelFeasibilityService#canReachInTime. Los voluntarios sin ubicación
     * no son descartados en este filtro.
     * 4. Calcula el score de cada voluntario mediante HelpRequestScoreEngine,
     * donde el tiempo de viaje es un factor determinante en la puntuación.
     * 5. Ordena los voluntarios por score descendente (mejor puntuación primero).
     * 6. Selecciona los mejores candidatos, limitado por MAX_PROPOSALS_PER_ENTITY.
     * 7. Filtra voluntarios que:
     * - Ya alcanzaron el máximo de proposals activas
     * - Están en periodo de cooldown
     * - Ya tienen una proposal para esta entidad
     * 8. Crea las proposals para los voluntarios que pasan todos los filtros.
     * Es el núcleo del motor de matching entre HelpRequest y voluntarios disponibles,
     * priorizando a aquellos que pueden llegar más rápido al destino.
     */
    public void generateForHelpRequest(HelpRequest helpRequest, int radiusMeters, int retryCount, boolean fromRetry) {

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
                        .map(v -> new VolunteerCandidate(v, distanceMap.get(v.getId())))
                        .filter(c -> {
                            double estimatedSeconds = getEstimatedSeconds(c);

                            // Si hay deadline se filtramos aquí (sin ORS)
                            if (helpRequest.getDeadline() != null) {
                                long availableSeconds = ChronoUnit.SECONDS.between(
                                        LocalDateTime.now(),
                                        helpRequest.getDeadline()
                                );

                                return estimatedSeconds < availableSeconds;
                            }

                            return true;
                        })
                        .sorted(Comparator.comparingDouble(VolunteerCandidate::distanceMeters))
                        .limit(proposalMatchingConfig.getMaxCandidatesPrefilter()) // ej: 50
                        .toList();

        List<VolunteerCandidate> candidatesForTravel =
                candidates.stream()
                        .limit(proposalMatchingConfig.getMaxCandidatesForTravel()) // ej: 20
                        .toList();

        log.info("[matching-helprequest] Volunteers found for HelpRequest {}: {} (limited to {} for travel calculation)",
                helpRequest.getId(),
                candidates.size(),
                proposalMatchingConfig.getMaxCandidatesForTravel());

        long afterPreload = System.currentTimeMillis();

        ExecutorService executor =
                Executors.newFixedThreadPool(proposalMatchingConfig.getTravelTimeThreads());

        Map<UUID, Double> travelTimes = calculateTravelTimesInParallel(candidatesForTravel, helpRequest.getLocation(), executor);

        shutdownExecutorWithTimeout(executor);

        if (helpRequest.getDeadline() != null) {
            int beforeFilterSize = candidates.size();
            candidates = candidates.stream()
                    .filter(c -> {
                        if (c.volunteer().getUser().getLocation() == null) {
                            // Si el voluntario no tiene ubicación, lo consideramos viable (no podemos descartarlo)
                            return true;
                        }
                        double travel = travelTimes.getOrDefault(c.volunteer().getId(), 0.0);
                        long secondsUntilDeadline = ChronoUnit.SECONDS.between(LocalDateTime.now(), helpRequest.getDeadline());
                        return travel < secondsUntilDeadline;
                    })
                    .toList();

            log.info("[matching-helprequest] Candidates after travel time feasibility filter: {} (filtered out {})",
                    candidates.size(),
                    beforeFilterSize - candidates.size());

            // Si después del filtro no quedan candidatos, terminamos temprano
            if (candidates.isEmpty()) {
                log.warn("[matching-helprequest] No volunteers can reach in time for HelpRequest {} with deadline {}",
                        helpRequest.getId(),
                        helpRequest.getDeadline());
                return;
            }
        }

        long afterTravelFilter = System.currentTimeMillis();

        Map<UUID, Long> pendingCounts = loadPendingCounts(volunteers);
        Map<UUID, LocalDateTime> lastResponses = loadLastResponses(volunteers);

        long afterCountsLoad = System.currentTimeMillis();

        List<Map.Entry<Volunteer, Double>> ranked =
                matchingEngine.rankCandidates(
                        helpRequest,
                        candidates,
                        helpRequestScoreEngine,
                        pendingCounts,
                        retryCount,
                        travelTimes
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
                existingProposals,
                fromRetry
        );

        long end = System.currentTimeMillis();

        log.debug(
                "[matching-helprequest] Matching result for HelpRequest {} -> candidates evaluated: {}, proposals created: {}, took {} ms",
                helpRequest.getId(),
                candidates.size(),
                created,
                System.currentTimeMillis() - start
        );

        log.debug(
                "[matching-timing-helprequest] fetch={}ms preload={}ms travel-time-filter={}ms counts-load={}ms ranking={}ms create={}ms total={}ms",
                (afterFetch - start),
                (afterPreload - afterFetch),
                (afterTravelFilter - afterPreload),
                (afterCountsLoad - afterTravelFilter),
                (afterRanking - afterCountsLoad),
                (end - afterRanking),
                (end - start)
        );
    }

    /**
     * Overload para el primer disparo desde el listener
     */
    public void generateForHelpRequest(HelpRequest helpRequest) {
        generateForHelpRequest(helpRequest, proposalMatchingConfig.getMaxRadiusDistance(), 0, true);
    }

    /**
     * Genera proposals para los mejores voluntarios disponibles para una Donation.
     * El proceso de matching sigue estos pasos:
     * 1. Obtiene voluntarios disponibles dentro del radio máximo configurado,
     * excluyendo al usuario que creó la donación.
     * 2. Calcula el tiempo de viaje estimado de cada voluntario al punto de la donación
     * (utilizando el medio de transporte del voluntario o FOOT_WALKING por defecto).
     * 3. Si la Donation tiene fecha de expiración (expiryDate), filtra los voluntarios que pueden llegar a tiempo
     * utilizando TravelFeasibilityService#canReachInTime. Los voluntarios sin ubicación
     * no son descartados en este filtro.
     * 4. Calcula el score de cada voluntario mediante DonationScoreEngine,
     * donde el tiempo de viaje es un factor determinante en la puntuación.
     * 5. Ordena los voluntarios por score descendente (mejor puntuación primero).
     * 6. Selecciona los mejores candidatos, limitado por MAX_PROPOSALS_PER_ENTITY.
     * 7. Filtra voluntarios que:
     * - Ya alcanzaron el máximo de proposals activas
     * - Están en periodo de cooldown
     * - Ya tienen una proposal para esta entidad
     * 8. Crea las proposals para los voluntarios que pasan todos los filtros.
     * Es el núcleo del motor de matching entre Donation y voluntarios disponibles,
     * priorizando a aquellos que pueden llegar más rápido al destino.
     */
    public void generateForDonation(Donation donation, int radiusMeters, int retryCount, boolean fromRetry) {

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

        List<Volunteer> volunteers =
                volunteerRepository.findAllByIdWithUser(distanceMap.keySet());

        List<VolunteerCandidate> candidates =
                volunteers.stream()
                        .map(v -> new VolunteerCandidate(v, distanceMap.get(v.getId())))
                        .filter(c -> {

                            double estimatedSeconds = getEstimatedSeconds(c);

                            // Si hay expiry → filtramos ya aquí SIN ORS
                            if (donation.getExpiryDate() != null) {
                                long availableSeconds = ChronoUnit.SECONDS.between(
                                        LocalDateTime.now(),
                                        donation.getExpiryDate()
                                );
                                return estimatedSeconds < availableSeconds;
                            }

                            return true;
                        })
                        .sorted(Comparator.comparingDouble(VolunteerCandidate::distanceMeters))
                        .limit(proposalMatchingConfig.getMaxCandidatesPrefilter())
                        .toList();

        List<VolunteerCandidate> candidatesForTravel =
                candidates.stream()
                        .limit(proposalMatchingConfig.getMaxCandidatesForTravel())
                        .toList();

        log.info("[matching-donation] Volunteers found for Donation {}: {} (limited to {} for travel calculation)",
                donation.getId(),
                candidates.size(),
                proposalMatchingConfig.getMaxCandidatesForTravel());

        long afterPreload = System.currentTimeMillis();

        ExecutorService executor =
                Executors.newFixedThreadPool(proposalMatchingConfig.getTravelTimeThreads());

        Map<UUID, Double> travelTimes = calculateTravelTimesInParallel(candidatesForTravel, donation.getLocation(), executor);

        shutdownExecutorWithTimeout(executor);

        if (donation.getExpiryDate() != null) {

            int beforeFilterSize = candidates.size();

            candidates = candidates.stream()
                    .filter(c -> {
                        if (c.volunteer().getUser().getLocation() == null) {
                            return true;
                        }

                        double travel = travelTimes.getOrDefault(c.volunteer().getId(), 0.0);

                        long secondsUntilExpiry = ChronoUnit.SECONDS.between(
                                LocalDateTime.now(),
                                donation.getExpiryDate()
                        );

                        return travel < secondsUntilExpiry;
                    })
                    .toList();

            log.info("[matching-donation] Candidates after travel time feasibility filter: {} (filtered out {})",
                    candidates.size(),
                    beforeFilterSize - candidates.size());

            if (candidates.isEmpty()) {
                log.warn("[matching-donation] No volunteers can reach in time for Donation {} with expiry date {}",
                        donation.getId(),
                        donation.getExpiryDate());
                return;
            }
        }

        long afterTravelFilter = System.currentTimeMillis();

        Map<UUID, Long> pendingCounts = loadPendingCounts(volunteers);
        Map<UUID, LocalDateTime> lastResponses = loadLastResponses(volunteers);

        long afterCountsLoad = System.currentTimeMillis();

        List<Map.Entry<Volunteer, Double>> ranked =
                matchingEngine.rankCandidates(
                        donation,
                        candidates,
                        donationScoreEngine,
                        pendingCounts,
                        retryCount,
                        travelTimes
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
                existingProposals,
                fromRetry
        );

        long end = System.currentTimeMillis();

        log.debug(
                "[matching-donation] Matching result for Donation {} -> candidates evaluated: {}, proposals created: {}, took {} ms",
                donation.getId(),
                candidates.size(),
                created,
                System.currentTimeMillis() - start
        );

        log.debug(
                "[matching-timing-donation] fetch={}ms preload={}ms travel-time-filter={}ms counts-load={}ms ranking={}ms create={}ms total={}ms",
                (afterFetch - start),
                (afterPreload - afterFetch),
                (afterTravelFilter - afterPreload),
                (afterCountsLoad - afterTravelFilter),
                (afterRanking - afterCountsLoad),
                (end - afterRanking),
                (end - start)
        );
    }

    private double getEstimatedSeconds(VolunteerCandidate c) {
        double distance = c.distanceMeters();

        TransportMode mode = c.volunteer().getTransportMode() != null
                ? c.volunteer().getTransportMode()
                : TransportMode.FOOT_WALKING;

        double speed = switch (mode) {
            case FOOT_WALKING -> proposalMatchingConfig.getWalkSpeed();
            case CYCLING_REGULAR -> proposalMatchingConfig.getBikeSpeed();
            case DRIVING_CAR -> proposalMatchingConfig.getCarSpeed();
        };

        return distance / speed;
    }

    /**
     * Overload para el primer disparo desde el listener
     */
    public void generateForDonation(Donation donation) {
        generateForDonation(donation, proposalMatchingConfig.getMaxRadiusDistance(), 0, true);
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
            Set<UUID> existingProposals,
            boolean fromRetry
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
                    score,
                    fromRetry
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

    /**
     * Calcula tiempos de viaje en paralelo para un conjunto de candidatos.
     * Lanza todas las tareas en paralelo primero.
     * Espera a que todas terminen de verdad en paralelo (sin esperar cada una individualmente).
     * Recoge los resultados al final.
     */
    private Map<UUID, Double> calculateTravelTimesInParallel(
            List<VolunteerCandidate> candidates,
            Object destination,
            ExecutorService executor) {

        Map<UUID, CompletableFuture<Double>> futures = candidates.stream()
                .filter(c -> c.volunteer().getUser().getLocation() != null)
                .collect(Collectors.toMap(
                        c -> c.volunteer().getId(),
                        c -> CompletableFuture.supplyAsync(() -> {
                                    try {
                                        return travelFeasibilityService.getEstimatedTravel(
                                                c.volunteer().getUser().getLocation(),
                                                (org.locationtech.jts.geom.Point) destination,
                                                c.volunteer().getTransportMode() != null
                                                        ? c.volunteer().getTransportMode()
                                                        : TransportMode.FOOT_WALKING
                                        ).getDuration();
                                    } catch (Exception e) {
                                        log.warn("[travel-time] Error calculating travel time for volunteer {}: {}",
                                                c.volunteer().getId(), e.getMessage());
                                        return 0.0;
                                    }
                                }, executor)
                                .exceptionally(ex -> {
                                    log.warn("[travel-time] Async exception: {}", ex.getMessage());
                                    return 0.0;
                                })
                ));

        CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0])).join();

        return futures.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().join()
                ));
    }

    /**
     * Ejecuta un ExecutorService con timeout garantizado.
     * - Intenta shutdown normal con 30 segundos
     * - Si falla, ejecuta shutdownNow()
     */
    private void shutdownExecutorWithTimeout(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                log.warn("[executor] Executor timeout, forcing shutdown");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.warn("[executor] Interrupt during executor shutdown: {}", e.getMessage());
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}
