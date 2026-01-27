package com.communityhelp.app.volunteer.repository;

import com.communityhelp.app.volunteer.model.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VolunteerRepository extends JpaRepository<Volunteer, UUID> {

    /**
     * Obtiene el voluntario asociado a un usuario.
     */
    Optional<Volunteer> findByUserId(UUID userId);

}
