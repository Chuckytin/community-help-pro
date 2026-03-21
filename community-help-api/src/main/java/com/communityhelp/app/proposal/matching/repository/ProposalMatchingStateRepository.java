package com.communityhelp.app.proposal.matching.repository;

import com.communityhelp.app.proposal.matching.model.ProposalMatchingState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProposalMatchingStateRepository extends JpaRepository<ProposalMatchingState, UUID> {
}
