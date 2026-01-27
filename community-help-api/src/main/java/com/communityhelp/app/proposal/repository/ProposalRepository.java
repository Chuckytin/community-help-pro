package com.communityhelp.app.proposal.repository;

import com.communityhelp.app.proposal.model.Proposal;
import com.communityhelp.app.proposal.model.ProposalType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, UUID> {

    /**
     * Obtiene propuestas por tipo y entidad objetivo (Donation o HelpRequest).
     */
    Page<Proposal> findByTypeAndTargetEntityId(
            ProposalType type,
            UUID targetEntityId,
            Pageable pageable
    );

    /**
     * Obtiene propuestas de un voluntario específico.
     */
    Page<Proposal> findByVolunteerId(
            UUID volunteerId,
            Pageable pageable
    );

}
