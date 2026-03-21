package com.communityhelp.app.volunteer.repository;

import com.communityhelp.app.volunteer.model.Volunteer;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VolunteerRepository extends JpaRepository<Volunteer, UUID> {

    /**
     * Obtiene el voluntario asociado a un usuario.
     */
    Optional<Volunteer> findByUser_Id(UUID userId);

    /**
     * Comprueba si existe el usuario por el id.
     */
    boolean existsByUser_Id(UUID userId);

    /**
     * Obtiene todos los ids de los voluntarios con join en User.
     */
    @Query("SELECT v FROM Volunteer v JOIN FETCH v.user WHERE v.id IN :ids")
    List<Volunteer> findAllByIdWithUser(Collection<UUID> ids);

    /**
     * Busca Voluntarios disponibles dentro de un radio de distancia con PostGIS
     */
    @Query(value = """
            SELECT
                v.user_id,
                ST_Distance(u.location, :location) AS distance
            FROM volunteers v
            JOIN users u ON v.user_id = u.id
            WHERE v.available = true
            AND ST_DWithin(u.location, :location, :radius)
            AND (
                v.radius_km IS NULL
                OR ST_Distance(u.location, :location) <= v.radius_km * 1000
            )
            ORDER BY u.location <-> :location
            """, nativeQuery = true)
    List<Object[]> findNearbyVolunteerIds(
            Point location,
            double radius,
            Pageable pageable
    );

    /**
     * Consulta futura cuando haya >50k voluntarios
     * Busca voluntarios cerca y devuelve los datos del id, distance, rating y skill_count.
     */
    @Query(value = """
            SELECT
                v.user_id AS volunteer_id,
                ST_Distance(u.location, :location) AS distance,
                COALESCE(u.rating, 0) AS rating,
                (
                    SELECT COUNT(*)
                    FROM volunteer_skills vs
                    WHERE vs.volunteer_id = v.user_id
                ) AS skill_count
            FROM volunteers v
            JOIN users u ON v.user_id = u.id
            WHERE v.available = true
            AND ST_DWithin(u.location, :location, :radius)
            AND (
                v.radius_km IS NULL
                OR ST_Distance(u.location, :location) <= v.radius_km * 1000
            )
            ORDER BY u.location <-> :location
            """, nativeQuery = true)
    List<Object[]> findNearbyVolunteerData(
            Point location,
            double radius,
            Pageable pageable
    );

}
