package com.communityhelp.app.review.repository;

import com.communityhelp.app.review.model.Review;
import com.communityhelp.app.review.model.ReviewContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    /**
     * Obtiene reviews por contexto y entidad relacionada (Donation o HelpRequest).
     */
    Page<Review> findByContextAndContextEntityId(
            ReviewContext context,
            UUID contextEntityId,
            Pageable pageable
    );

}
