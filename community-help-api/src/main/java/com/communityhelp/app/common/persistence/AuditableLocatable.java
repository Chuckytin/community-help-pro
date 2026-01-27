package com.communityhelp.app.common.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

/**
 * Clase base para las entidades que requieren ambas auditorias:
 * - createdAt/updatedAt
 * - Geolocation de location
 */
@MappedSuperclass
@Getter
public abstract class AuditableLocatable extends Auditable {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    /**
     * Ubicación del usuario para eL matching geográfico
     */
    @Column(name = "location", columnDefinition = "geography(POINT,4326)")
    @JdbcTypeCode(SqlTypes.GEOGRAPHY)
    private Point location;

    /**
     * Helper del latitude.
     * Transient para que no forme parte del estado persistente de la entidad
     * (vive solo en memoria, no en la BBDD)
     */
    @Transient
    public Double getLatitude() {
        return location != null ? location.getY() : null;
    }

    /**
     * Helper del longitude.
     * Transient para que no forme parte del estado persistente de la entidad
     * (vive solo en memoria, no en la BBDD)
     */
    @Transient
    public Double getLongitude() {
        return location != null ? location.getX() : null;
    }

    /**
     * Helper para setear desde el frontend latitude y longitude.
     */
    public void setLocation(double latitude, double longitude) {
        this.location = GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
        this.location.setSRID(4326);
    }

}
