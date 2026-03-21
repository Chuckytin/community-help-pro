package com.communityhelp.app.proposal.matching.service;

import com.communityhelp.app.proposal.config.ProposalMatchingConfig;
import com.communityhelp.app.proposal.matching.model.ProposalMatchingState;
import com.communityhelp.app.proposal.matching.repository.ProposalMatchingStateRepository;
import com.communityhelp.app.proposal.model.ProposalType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProposalMatchingStateService {

    private final ProposalMatchingStateRepository stateRepository;
    private final ProposalMatchingConfig config;

    /**
     * Inicializa el estado de matching al crear una entidad.
     */
    @Transactional
    public void initState(UUID entityId, ProposalType type) {
        ProposalMatchingState state = new ProposalMatchingState();
        state.setEntityId(entityId);
        state.setEntityType(type);
        state.setCurrentRadiusMeters(config.getMaxRadiusDistance());
        state.setRetryCount(0);
        state.setLastRetryAt(LocalDateTime.now());
        stateRepository.save(state);
    }

    /**
     * Devuelve el estado actualizado para el siguiente intento de matching, incrementando el radio y el contador de reintentos.
     */
    @Transactional
    public ProposalMatchingState getNextState(UUID entityId, ProposalType type) {
        ProposalMatchingState state = stateRepository
                .findById(entityId)
                .orElseGet(() -> createDefaultState(entityId, type));

        int nextRadius = Math.min(
                state.getCurrentRadiusMeters() + config.getRadiusExpansionStep(),
                config.getMaxExpandedRadius()
        );

        state.setCurrentRadiusMeters(nextRadius);
        state.setRetryCount(state.getRetryCount() + 1);
        state.setLastRetryAt(LocalDateTime.now());

        return stateRepository.save(state);
    }

    /**
     * Fallback para el caso en que getNextRadius se llame y no exista un estado previo.
     */
    private ProposalMatchingState createDefaultState(UUID entityId, ProposalType type) {
        ProposalMatchingState state = new ProposalMatchingState();
        state.setEntityId(entityId);
        state.setEntityType(type);
        state.setCurrentRadiusMeters(config.getMaxRadiusDistance());
        state.setRetryCount(0);
        state.setLastRetryAt(LocalDateTime.now());
        return stateRepository.save(state);
    }

    /**
     * Limpia el estado cuando la entidad queda resuelta.
     */
    @Transactional
    public void clearState(UUID entityId) {
        stateRepository.deleteById(entityId);
    }
}
