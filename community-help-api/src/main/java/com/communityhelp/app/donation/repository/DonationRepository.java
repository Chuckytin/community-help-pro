package com.communityhelp.app.donation.repository;

import com.communityhelp.app.donation.model.Donation;
import com.communityhelp.app.donation.model.DonationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DonationRepository extends JpaRepository<Donation, UUID> {

    /**
     * Obtiene todas las donaciones de un usuario
     */
    Page<Donation> findByDonor_Id(UUID donorId, Pageable pageable);

    List<Donation> findByDonor_Id(UUID id);

    /**
     * Obtiene todas las donaciones de un usuario por estado
     */
    Page<Donation> findByDonor_IdAndStatus(UUID donorId, DonationStatus donationStatus, Pageable pageable);

    /**
     * Obtiene todas las donaciones de un voluntario
     */
    Page<Donation> findByVolunteer_Id(UUID volunteerId, Pageable pageable);
    List<Donation> findByVolunteer_Id(UUID id);

    /**
     * Obtiene todas las donaciones disponibles cercanas.
     */
    @Query(value = """
            SELECT d.* FROM donations d
            WHERE d.status = 'AVAILABLE'
            AND d.active = true
            AND (:type IS NULL OR d.donation_type = :type)
            AND ST_DWithin(
                d.location::geography,
                ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
                :radiusMeters
            )
            ORDER BY ST_Distance(
                d.location::geography,
                ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography
            )
            """, nativeQuery = true)
    Page<Donation> findNearbyAvailable(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radiusMeters") double radiusMeters,
            @Param("type") String type,
            Pageable pageable
    );

    /**
     * Libera las donaciones donde el usuario era volunteer
     */
    @Modifying
    @Query("UPDATE Donation d SET d.status = 'AVAILABLE', d.volunteer = null WHERE d.volunteer.id = :userId")
    void releaseDonationsAsVolunteer(@Param("userId") UUID userId, @Param("reason") String reason);

    /**
     * Cancela todas las donaciones donde el usuario era el donor
     */
    @Modifying
    @Query("UPDATE Donation d SET d.status = 'CANCELLED', d.cancelReason = :reason WHERE d.donor.id = :userId")
    void releaseDonationsAsDonor(@Param("userId") UUID userId, @Param("reason") String reason);

    /**
     * Obtiene todas las donaciones activas (no eliminadas) para mostrar en el feed general. Esto se puede usar para filtrar las donaciones
     * que ya han sido marcadas como inactivas o eliminadas,
     */
    @Query("SELECT d FROM Donation d WHERE d.active = true")
    List<Donation> findAllActive();

    /**
     * Comprueba si ya existe una donación con el mismo título (ignorando mayúsculas) para un mismo donante y estado. Esto se puede usar para evitar que un usuario cree dos donaciones
     * con el mismo título y estado, lo cual podría ser confuso.
     */
    boolean existsByDonor_IdAndTitleIgnoreCaseAndStatus(UUID donorId, String title, DonationStatus status);
}
