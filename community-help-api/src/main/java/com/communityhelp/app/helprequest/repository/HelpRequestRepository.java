package com.communityhelp.app.helprequest.repository;

import com.communityhelp.app.donation.model.Donation;
import com.communityhelp.app.helprequest.model.HelpRequest;
import com.communityhelp.app.helprequest.model.HelpRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface HelpRequestRepository extends JpaRepository<HelpRequest, UUID> {

    /**
     * Obtiene todas las solicitudes de un usuario
     */
    List<HelpRequest> findByRequester_Id(UUID id);

    /**
     * Obtiene todas las solicitudes de un usuario
     */
    Page<HelpRequest> findByRequester_Id(UUID requesterId, Pageable pageable);

    /**
     * Obtiene todas las solicitudes de un usuario filtradas por estado
     */
    Page<HelpRequest> findByRequester_IdAndStatus(UUID requesterId, HelpRequestStatus status, Pageable pageable);

    /**
     * Obtiene todas las solicitudes aceptadas por un voluntario
     */
    List<HelpRequest> findByVolunteer_Id(UUID volunteerId);

    /**
     * Filtra HelpRequests por estado
     */
    Page<HelpRequest> findByStatus(HelpRequestStatus status, Pageable pageable);

    /**
     * Filtra HelpRequests por estado y fecha de vencimiento futura (para evitar mostrar tareas vencidas)
     */
    Page<HelpRequest> findByStatusAndDeadlineAfter(HelpRequestStatus status, LocalDateTime localDateTime, Pageable pageable);

    /**
     * Filtra por tareas activas o aceptadas para un voluntario específico
     */
    Page<HelpRequest> findByVolunteer_IdAndStatus(UUID volunteerId, HelpRequestStatus status, Pageable pageable);

    /**
     * Filtra por tareas activas o aceptadas para un voluntario específico sin importar el estado
     */
    Page<HelpRequest> findByVolunteer_Id(UUID volunteerId, Pageable pageable);

    /**
     * Obtiene todas las solicitudes abiertas cercanas.
     */
    @Query(value = """
            SELECT hr.* FROM help_requests hr
            WHERE hr.status = 'OPEN'
            AND hr.active = true
            AND (hr.deadline IS NULL OR hr.deadline > NOW())
            AND (:type IS NULL OR hr.type = :type)
            AND ST_DWithin(
                hr.location::geography,
                ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
                :radiusMeters
            )
            ORDER BY ST_Distance(
                hr.location::geography,
                ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography
            )
            """, nativeQuery = true)
    Page<HelpRequest> findNearbyOpen(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radiusMeters") double radiusMeters,
            @Param("type") String type,
            Pageable pageable
    );

    /**
     * Libera todas las helpRequests de un volunteer antes de borrarlo
     */
    @Modifying
    @Query("UPDATE HelpRequest h SET h.status = 'OPEN', h.volunteer = null WHERE h.volunteer.id = :userId")
    void releaseHelpRequestsAsVolunteer(@Param("userId") UUID userId, @Param("reason") String reason);

    /**
     * Cancela todas las helRequests de un solicitante antes de borrarlo.
     */
    @Modifying
    @Query("UPDATE HelpRequest h SET h.status = 'CANCELLED', h.cancelReason = :reason WHERE h.requester.id = :userId")
    void releaseHelpRequestsAsRequester(@Param("userId") UUID userId, @Param("reason") String reason);

    @Query("SELECT h FROM HelpRequest h WHERE h.active = true")
    List<HelpRequest> findAllActive();
}

