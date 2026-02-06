package com.communityhelp.app.donation.repository;

import com.communityhelp.app.donation.model.Donation;
import com.communityhelp.app.donation.model.DonationStatus;
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
    List<Donation> findByDonor_Id(UUID donorId);

    /**
     * Obtiene todas las donaciones de un usuario por estado
     */
    List<Donation> findByDonor_IdAndStatus(UUID donorId, DonationStatus donationStatus);

    /**
     * Obtiene todas las donaciones de un voluntario
     */
    List<Donation> findByVolunteer_Id(UUID volunteerId);

    /**
     * Obtiene todas las solicitudes aceptadas por un voluntario
     */
    List<Donation> findByVolunteerId(UUID volunteerId);

    /**
     * Filtra por estado
     */
    List<Donation> findByStatus(DonationStatus status);

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

}
