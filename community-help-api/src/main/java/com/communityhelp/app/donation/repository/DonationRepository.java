package com.communityhelp.app.donation.repository;

import com.communityhelp.app.donation.model.Donation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DonationRepository extends JpaRepository<Donation, UUID> {

    /**
     * Obtiene donaciones asignadas a un voluntario.
     */
    Page<Donation> findByVolunteerId(UUID volunteerId, Pageable pageable);

}
