package com.communityhelp.app.review.model;

import com.communityhelp.app.common.persistence.Auditable;
import com.communityhelp.app.donation.model.Donation;
import com.communityhelp.app.helprequest.model.HelpRequest;
import com.communityhelp.app.user.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.Objects;
import java.util.UUID;

/**
 * Index - para cuando haya muchas reviews, consultar por target, donation, helprequest
 * UniqueConstraint - un mismo author solo puede dejar 1 review
 */
@Entity
@Table(
        name = "reviews",
        indexes = {
                @Index(name = "idx_review_target", columnList = "target_id"),
                @Index(name = "idx_review_author", columnList = "author_id"),
                @Index(name = "idx_review_donation", columnList = "donation_id"),
                @Index(name = "idx_review_help_request", columnList = "help_request_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"author_id", "donation_id"}),
                @UniqueConstraint(columnNames = {"author_id", "help_request_id"})
        }
)

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
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id")
    private User author;

    /**
     * A quién se reseña
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_id")
    private User target;

    /**
     * Puede ser nulo porque luego en el service se valida
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donation_id")
    private Donation donation;

    /**
     * Puede ser nulo porque luego en el service se valida
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "help_request_id")
    private HelpRequest helpRequest;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer rating;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String comment;

    /**
     * Helper del User que devuelve su identificador.
     * Transient para que no forme parte del estado persistente de la entidad
     * (vive solo en memoria, no en la BBDD)
     */
    @Transient
    public UUID getAuthorId() {
        return author != null ? author.getId() : null;
    }

    @Transient
    public UUID getTargetId() {
        return target != null ? target.getId() : null;
    }

    /**
     * Helper del User que devuelve su nombre.
     * Transient para que no forme parte del estado persistente de la entidad
     * (vive solo en memoria, no en la BBDD)
     */
    @Transient
    public String getAuthorName() {
        return author != null ? author.getName() : null;
    }

    @Transient
    public String getTargetName() {
        return target != null ? target.getName() : null;
    }

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
