package com.communityhelp.app.common.location;

import org.locationtech.jts.geom.Point;

public interface DistanceService {

    /**
     * Calcula la distancia en metros entre dos puntos geográficos.
     */
    double distanceMeters(Point from, Point to);

}