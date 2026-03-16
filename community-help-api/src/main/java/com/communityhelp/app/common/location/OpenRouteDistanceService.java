package com.communityhelp.app.common.location;

import com.communityhelp.app.common.openroute.model.TransportMode;
import com.communityhelp.app.common.openroute.service.OpenRouteService;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

/**
 * Implementación de DistanceService que utiliza OpenRouteService
 * para obtener distancias reales basadas en rutas.
 * - Da resultados más precisos que el cálculo geométrico.
 */
@Service
@RequiredArgsConstructor
public class OpenRouteDistanceService implements DistanceService {

    private final OpenRouteService openRouteService;

    /**
     * Obtiene la distancia real entre dos ubicaciones usando la API externa.
     */
    @Override
    public double distanceMeters(Point from, Point to) {

        return openRouteService
                .getTravelTime(from, to, TransportMode.DRIVING_CAR)
                .getDistance();
    }
}
