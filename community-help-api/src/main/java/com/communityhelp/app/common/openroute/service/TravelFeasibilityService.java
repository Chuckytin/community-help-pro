package com.communityhelp.app.common.openroute.service;

import com.communityhelp.app.common.openroute.dto.FastestTravelResponse;
import com.communityhelp.app.common.openroute.dto.TravelTimeResponse;
import com.communityhelp.app.common.openroute.model.TransportMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Servicio de negocio que evalúa si un voluntario puede llegar a tiempo
 * a una donación o solicitud de ayuda antes de su fecha límite.
 * Usa {@link OpenRouteService} para obtener el tiempo de viaje real.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TravelFeasibilityService {

    private final OpenRouteService openRouteService;

    /**
     * Calcula si un voluntario puede llegar al destino antes del deadline.
     * - Obtiene el tiempo de viaje estimado usando OpenRouteService.
     * - Compara el tiempo de viaje con el tiempo restante hasta el deadline.
     * Si la API falla, no bloqueamos — dejamos pasar, asumiendo que el voluntario podría llegar a tiempo.
     */
    public boolean canReachInTime(Point from, Point to,
                                  LocalDateTime deadline, TransportMode mode) {
        try {
            TravelTimeResponse travel = openRouteService.getTravelTime(from, to, mode);
            long secondsUntilDeadline = ChronoUnit.SECONDS.between(LocalDateTime.now(), deadline);
            return travel.getDuration() < secondsUntilDeadline;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Devuelve el tiempo de viaje estimado entre dos puntos.
     * Útil para mostrar información al voluntario antes de aceptar una tarea.
     * Si la API falla, devuelve un tiempo de viaje de 0, lo que puede interpretarse como "desconocido".
     */
    public TravelTimeResponse getEstimatedTravel(Point from, Point to, TransportMode mode) {
        try {
            return openRouteService.getTravelTime(from, to, mode);
        } catch (Exception e) {
            return new TravelTimeResponse(0, 0);
        }
    }

    /**
     * Calcula el tiempo de viaje para todos los modos de transporte disponibles y devuelve el más rápido junto con el modo usado.
     * Si la API falla para todos los modos, devuelve un tiempo de viaje de 0 y modo null.
     */
    public FastestTravelResponse getFastestTravel(Point from, Point to) {
        TransportMode fastestMode = null;
        TravelTimeResponse fastestTravel = null;

        for (TransportMode mode : TransportMode.values()) {
            try {
                TravelTimeResponse travel = openRouteService.getTravelTime(from, to, mode);
                if (fastestTravel == null || travel.getDuration() < fastestTravel.getDuration()) {
                    fastestTravel = travel;
                    fastestMode = mode;
                }
            } catch (Exception e) {
                log.warn("[TravelFeasibility] Mode {} is not available for this route", mode);
            }
        }

        if (fastestTravel == null) {
            return new FastestTravelResponse(0, 0, null);
        }

        return new FastestTravelResponse(
                fastestTravel.getDistance(),
                fastestTravel.getDuration(),
                fastestMode
        );
    }

}