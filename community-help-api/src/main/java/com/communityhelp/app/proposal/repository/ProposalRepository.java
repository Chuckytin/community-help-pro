package com.communityhelp.app.proposal.repository;

import com.communityhelp.app.proposal.model.Proposal;
import com.communityhelp.app.proposal.model.ProposalStatus;
import com.communityhelp.app.proposal.model.ProposalType;
import com.communityhelp.app.volunteer.model.Volunteer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, UUID> {

    /**
     * Obtiene proposals de un voluntario con lazy-loading optimizado de Volunteer.user.
     */
    @EntityGraph(attributePaths = {"volunteer", "volunteer.user"})
    Page<Proposal> findByVolunteer(Volunteer volunteer, Pageable pageable);

    /**
     * Obtiene proposals por estado con paginación.
     */
    Page<Proposal> findByStatus(ProposalStatus status, Pageable pageable);

    /**
     * Obtiene una proposal por entidad objetivo y voluntario.
     */
    Optional<Proposal> findByTargetEntityIdAndVolunteer_Id(UUID targetEntityId, UUID volunteerId);

    /**
     * Obtiene proposals ordenadas por score descendente para el ranking del voluntario.
     */
    List<Proposal> findAllByVolunteer_IdOrderByScoreDesc(UUID volunteerId);

    /**
     * Obtiene proposals asociadas a una entidad, consulta el historial completo de proposals.
     */
    List<Proposal> findAllByTargetEntityId(UUID targetEntityId);

    /**
     * Cncela las proposals asociadas si cancela una HelpRequest o una Donation.
     */
    @Modifying
    @Query("""
            UPDATE Proposal p
            SET p.status = 'CANCELLED'
            WHERE p.targetEntityId = :entityId
            AND p.status = 'PENDING'
            """)
    void cancelPendingProposals(UUID entityId);

    /**
     * Obtiene todas las proposals pendientes de una entidad,
     * excepto las de un voluntario específico (el que aceptó la proposal).
     */
    @Query("""
            SELECT p FROM Proposal p
            WHERE p.targetEntityId = :entityId
              AND p.status = 'PENDING'
              AND p.volunteer.id <> :acceptedVolunteerId
            """)
    List<Proposal> findPendingByTargetEntityExcludingVolunteer(UUID entityId, UUID acceptedVolunteerId);

    /**
     * Cuenta el número de proposals por estado para múltiples voluntarios.
     * Retorna una lista de arrays con el UUID del voluntario y la cantidad de proposals.
     */
    @Query("""
            SELECT p.volunteer.id, COUNT(p)
            FROM Proposal p
            WHERE p.status = :status
            AND p.volunteer.id IN :volunteerIds
            GROUP BY p.volunteer.id
            """)
    List<Object[]> countByVolunteerIdsAndStatus(List<UUID> volunteerIds, ProposalStatus status);

    /**
     * Obtiene la fecha del último respondedAt para cada voluntario en la lista.
     * Optimiza el periodo de cooldown, permitiendo consultar las últimas respuestas de múltiples voluntarios.
     * Retorna una lista de arrays con el UUID del voluntario y la fecha de la última respuesta.
     */
    @Query("""
            SELECT p.volunteer.id, MAX(p.respondedAt)
            FROM Proposal p
            WHERE p.volunteer.id IN :volunteerIds
            AND p.respondedAt IS NOT NULL
            GROUP BY p.volunteer.id
            """)
    List<Object[]> findLastResponsesByVolunteerIds(List<UUID> volunteerIds);

    /**
     * Obtiene los IDs de los voluntarios que ya tienen una proposal ACTIVA
     * (PENDING o ACCEPTED) para una entidad específica.
     * Excluye CANCELLED, REJECTED y EXPIRED para permitir el retry.
     */
    @Query("""
            SELECT p.volunteer.id
            FROM Proposal p
            WHERE p.targetEntityId = :entityId
              AND p.status IN ('PENDING', 'ACCEPTED')
            """)
    List<UUID> findVolunteerIdsWithProposal(UUID entityId);

    /**
     * Obtiene las distintas entidades para el retry de las proposals.
     */
    @Query("""
                SELECT DISTINCT p.targetEntityId
                FROM Proposal p
                WHERE p.type = :type
                AND p.status = :status
                AND p.createdAt < :threshold
            """)
    Set<UUID> findDistinctTargetEntityIdsForRetry(
            ProposalType type,
            ProposalStatus status,
            LocalDateTime threshold
    );

    /**
     * Expira una Propuesta pendiente donde el threshold supera al createdAt
     */
    @Modifying
    @Query("""
            UPDATE Proposal p
            SET p.status = 'EXPIRED'
            WHERE p.type = :type
              AND p.status = 'PENDING'
              AND p.createdAt < :threshold
            """)
    void expireStaleProposals(ProposalType type, LocalDateTime threshold);

    /**
     * Reactiva una proposal expirada para el retry, actualizando su score y estado a PENDING.
     */
    @Modifying
    @Query("""
            UPDATE Proposal p
            SET p.status = 'PENDING',
                p.score = :score,
                p.respondedAt = NULL
            WHERE p.volunteer.id = :volunteerId
              AND p.targetEntityId = :entityId
              AND p.type = :type
              AND p.status = 'EXPIRED'
            """)
    int reactivateExpiredProposal(
            UUID volunteerId,
            UUID entityId,
            ProposalType type,
            double score
    );

}
