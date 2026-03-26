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
     * Filtra por estado
     */
    Page<Donation> findByStatus(DonationStatus status, Pageable pageable);

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

    @Query("SELECT d FROM Donation d WHERE d.active = true")
    List<Donation> findAllActive();
}
