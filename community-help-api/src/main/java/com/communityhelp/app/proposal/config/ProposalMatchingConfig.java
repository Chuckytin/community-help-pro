package com.communityhelp.app.proposal.config;

/**
 * Configuración central del motor de matching.
 */
public final class ProposalMatchingConfig {

    /**
     * Máximo número de proposals activas por voluntario.
     */
    public static final int MAX_ACTIVE_PROPOSALS = 5;

    /**
     * Minutos de espera antes de volver a recomendar algo
     * a un voluntario que rechazó una proposal.
     */
    public static final int PROPOSAL_COOLDOWN_MINUTES = 30;

    /**
     * Minutos de espera para reintentar generar proposals
     * cuando nadie acepta una recomendación.
     */
    public static final int RETRY_DELAY_MINUTES = 20;

    /**
     * Número máximo de voluntarios a los que enviar proposals.
     */
    public static final int MAX_PROPOSALS_PER_ENTITY = 5;

    /**
     * Distancia máxima de radio en metros.
     */
    public static final int MAX_RADIUS_DISTANCE = 20000;

    /**
     * Puntuación máxima de score por distancia.
     */
    public static final double MAX_DISTANCE_SCORE = 30;

    /**
     * Puntuación máxima por distancia
     */
    public static final double MAX_WEIGHT_DISTANCE = 0.5;

    /**
     * Puntuación máxima por skills
     */
    public static final double MAX_WEIGHT_SKILLS = 0.35;

    /**
     * Puntuación máxima por rating
     */
    public static final double MAX_WEIGHT_RATING = 0.10;

    /**
     * Puntuación del peso máximo del factor de la carga de trabajo del voluntario.
     */
    public static final double MAX_WEIGHT_LOAD = 0.05;

    /**
     * Para traer más candidatos de los necesarios
     */
    public static final int MAX_CANDIDATES_MULTIPLIER = 6;

    private ProposalMatchingConfig() {}
}