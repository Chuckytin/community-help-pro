package com.communityhelp.app.helprequest.repository;

import com.communityhelp.app.helprequest.model.HelpRequest;
import com.communityhelp.app.helprequest.model.HelpRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HelpRequestRepository extends JpaRepository<HelpRequest, UUID> {

    /**
     * Obtiene todas las solicitudes de un usuario
     */
    List<HelpRequest> findByRequester_Id(UUID requesterId);

    /**
     * Obtiene todas las solicitudes aceptadas por un voluntario
     */
    List<HelpRequest> findByVolunteerId(UUID volunteerId);

    /**
     * Filtra por estado
     */
    List<HelpRequest> findByStatus(HelpRequestStatus status);

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
}

