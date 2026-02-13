package com.communityhelp.app.review.repository;

import com.communityhelp.app.review.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    /**
     * Verifica si el User author de la review es a una Donation
     */
    boolean existsByAuthor_IdAndDonation_Id(UUID authorId, UUID donationId);

    /**
     * Verifica si el User author de la review es a una HelpRequest
     */
    boolean existsByAuthor_IdAndHelpRequest_Id(UUID authorId, UUID helpRequestId);

    /**
     * Obtiene la review con el author y el target
     */
    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.author LEFT JOIN FETCH r.target WHERE r.id = :id")
    Optional<Review> findWithAuthorAndTargetById(UUID id);

    /**
     * Obtiene todas las Review de un User target
     */
    @Query("SELECT r FROM Review r JOIN FETCH r.author JOIN FETCH r.target WHERE r.target.id = :targetId")
    List<Review> findByTarget_Id(UUID targetId);

    /**
     * Selecciona la media de las Review de un User target
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.target.id = :userId")
    Double calculateAverageRatingByTargetId(UUID userId);

}
