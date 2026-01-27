package com.communityhelp.app.review.model;

import com.communityhelp.app.common.persistence.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "reviews")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Review extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Quién escribe la reseña
     */
    @Column(name = "reviewer_id", nullable = false)
    private UUID reviewerId;

    /**
     * A quién se reseña
     */
    @Column(name = "reviewed_user_id", nullable = false)
    private UUID reviewedUserId;

    /**
     * Contexto de la reseña.
     * - DONATION
     * - HELP_REQUEST
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewContext context;

    /**
     * ID de la entidad relacionada (puede ser un Donation o un HelpRequest)
     */
    @Column(name = "context_entity_id")
    private UUID contextEntityId;

    @Column(nullable = false)
    private Integer rating;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String comment;



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return Objects.equals(id, review.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
