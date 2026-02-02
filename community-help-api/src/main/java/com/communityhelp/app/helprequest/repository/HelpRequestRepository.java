package com.communityhelp.app.helprequest.repository;

import com.communityhelp.app.helprequest.model.HelpRequest;
import com.communityhelp.app.helprequest.model.HelpRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
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

}

