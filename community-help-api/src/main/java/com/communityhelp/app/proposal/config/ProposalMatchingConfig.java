package com.communityhelp.app.proposal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuración central del motor de matching.
 */
@ConfigurationProperties(prefix = "proposal.matching")
@Component
@Getter
@Setter
public class ProposalMatchingConfig {

    /**
     * Máximo número de proposals activas por voluntario.
     */
    public int maxActiveProposals = 5;

    /**
     * Minutos de espera antes de volver a recomendar algo
     * a un voluntario que rechazó una proposal.
     */
    public int proposalCooldownMinutes = 20;

    /**
     * Minutos de espera para reintentar generar proposals
     * cuando nadie acepta una recomendación.
     */
    public int retryDelayMinutes = 30;

    /**
     * Número máximo de voluntarios a los que enviar proposals.
     */
    public int maxProposalsPerEntity = 5;

    /**
     * Distancia máxima de radio en metros.
     */
    public int maxRadiusDistance = 10000;

    /**
     * Puntuación máxima de score por distancia.
     */
    public double maxDistanceScore = 30;

    /**
     * Puntuación máxima por distancia
     */
    public double maxWeightDistance = 0.5;

    /**
     * Puntuación máxima por skills
     */
    public double maxWeightSkills = 0.35;

    /**
     * Puntuación máxima por rating
     */
    public double maxWeightRating = 0.10;

    /**
     * Puntuación del peso máximo del factor de la carga de trabajo del voluntario.
     */
    public double maxWeightLoad = 0.05;

    /**
     * Para traer más candidatos de los necesarios
     */
    public int maxCandidatesMultiplier = 6;

    /**
     * Número máximo de candidatos a evaluar para cada propuesta, incluso si el multiplicador sugiere traer más.
     * Esto ayuda a limitar el tiempo de procesamiento y evitar evaluar demasiados candidatos en casos extremos.
     */
    public int maxCandidatesForTravel = 20;

    /**
     * Número máximo de candidatos a prefiltrar por distancia antes de calcular tiempos de viaje.
     */
    private int maxCandidatesPrefilter = 50;

    /**
     * Número de threads para calcular tiempos de viaje en paralelo.
     * Ajustar según la capacidad del servidor y la latencia de la API de rutas.
     */
    private int travelTimeThreads = 10;

    /**
     * Velocidades medias (m/s) por modo de transporte
     */
    private double walkSpeed = 1.4;   // ~5 km/h
    private double bikeSpeed = 4.1;   // ~15 km/h
    private double carSpeed = 13.9;   // ~50 km/h

    /**
     * Radio de expansión en metros para buscar más voluntarios si no se encuentran suficientes candidatos.
     */
    private int radiusExpansionStep = 10000;

    /**
     * Radio de expansión máximo en metros para buscar voluntarios adicionales si no se encuentran suficientes candidatos dentro del radio inicial.
     */
    private int maxExpandedRadius = 50000;

}