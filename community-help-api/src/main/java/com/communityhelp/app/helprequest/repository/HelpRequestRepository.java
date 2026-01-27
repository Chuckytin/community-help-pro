package com.communityhelp.app.helprequest.repository;

import com.communityhelp.app.helprequest.model.HelpRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HelpRequestRepository extends JpaRepository<HelpRequest, UUID> {

    /**
     * Obtiene solicitudes de ayuda asignadas a un voluntario.
     */
    Page<HelpRequest> findByVolunteerId(UUID volunteerId, Pageable pageable);

}

