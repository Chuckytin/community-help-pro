package com.communityhelp.app.common.location;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Implementación resiliente de DistanceService.
 * Calcula la distancia mediante OpenRouteService, si falla utiliza el cálculo Haversine.
 * - @Primary para ser la implementación por defecto.
 */
@Service
@RequiredArgsConstructor
@Primary
public class ResilientDistanceService implements DistanceService {

    private final OpenRouteDistanceService openRoute;
    private final HaversineDistanceService fallback;

    @Override
    public double distanceMeters(Point from, Point to) {

        try {
            return openRoute.distanceMeters(from, to);
        } catch (Exception ex) {
            return fallback.distanceMeters(from, to);
        }
    }
}
