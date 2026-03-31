package com.communityhelp.app.common.openroute.service;

import com.communityhelp.app.common.openroute.dto.FastestTravelResponse;
import com.communityhelp.app.common.openroute.dto.TravelTimeResponse;
import com.communityhelp.app.common.openroute.model.TransportMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

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
     * Devuelve el tiempo de viaje estimado entre dos puntos.
     * Útil para mostrar información al voluntario antes de aceptar una tarea.
     * Si la API falla, devuelve un tiempo de viaje de 0, lo que puede interpretarse como "desconocido".
     */
    @Cacheable(
            value = "travelTime",
            key = "T(Math).round(#from.y * 1000) + '_' + T(Math).round(#from.x * 1000) + '_' " +
                    "+ T(Math).round(#to.y * 1000) + '_' + T(Math).round(#to.x * 1000) + '_' + #mode"
    )
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
    @Cacheable(
            value = "travelTime",
            key = "'fastest_' + T(Math).round(#from.y * 1000) + '_' + T(Math).round(#from.x * 1000) + '_' " +
                    "+ T(Math).round(#to.y * 1000) + '_' + T(Math).round(#to.x * 1000)"
    )
    public FastestTravelResponse getFastestTravel(Point from, Point to) {
        TransportMode fastestMode = null;
        TravelTimeResponse fastestTravel = null;

        for (TransportMode mode : TransportMode.values()) {
            try {
                TravelTimeResponse travel = getEstimatedTravel(from, to, mode);
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