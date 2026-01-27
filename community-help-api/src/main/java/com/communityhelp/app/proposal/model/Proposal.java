package com.communityhelp.app.proposal.model;

import com.communityhelp.app.common.persistence.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "proposals")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Proposal extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Tipo de propuesta.
     * - DONATION
     * - HELP_rEQUEST
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProposalType type;

    /**
     * ID de la entidad relacionada (Donation o HelpRequest)
     */
    @Column(name = "target_entity_id", nullable = false)
    private UUID targetEntityId;

    /**
     * ID del voluntario destinatario de la propuesta
     */
    @Column(name = "volunteer_id", nullable = false)
    private UUID volunteerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProposalStatus status;

    /**
     * Puntuación o prioridad que la IA le da a esta propuesta
     */
    private Double score;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Proposal proposal = (Proposal) o;
        return Objects.equals(id, proposal.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
