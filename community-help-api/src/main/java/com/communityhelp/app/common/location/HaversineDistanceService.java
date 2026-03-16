package com.communityhelp.app.common.location;

import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

/**
 * Implementación local de DistanceService basada en la fórmula de Haversine.
 * - Fallback para cuando los servicios externos no estén disponibles.
 * - No requiere llamadas HTTP y siempre está disponible.
 */
@Service
public class HaversineDistanceService implements DistanceService {

    private static final int EARTH_RADIUS = 6371;

    /**
     * Calcula la distancia aproximada entre dos puntos usando cálculo esférico.
     */
    @Override
    public double distanceMeters(Point from, Point to) {

        double lat1 = from.getY();
        double lon1 = from.getX();
        double lat2 = to.getY();
        double lon2 = to.getX();

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(dLat/2) * Math.sin(dLat/2)
                        + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon/2) * Math.sin(dLon/2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return EARTH_RADIUS * c * 1000;
    }
}
