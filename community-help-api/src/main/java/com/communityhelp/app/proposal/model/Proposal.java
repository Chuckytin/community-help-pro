package com.communityhelp.app.proposal.model;

import com.communityhelp.app.common.persistence.Auditable;
import com.communityhelp.app.volunteer.model.Volunteer;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa una Proposal generada por matching.
 * La Proposal se generará automáticamente a partir de scoring evaluando la distancia, disponibilidad y habilidades.
 * @Index - Para optimizar las consultas en la búsqueda de una HelpRequest/Donation, Volunteer y status.
 * @UniqueConstraint - Asegura que el mismo voluntario no pueda recibir más de una propuesta de la misma HelpRequest/Donation.
 */
@Entity
@Table(
        name = "proposals",
        indexes = {
                @Index(name = "idx_proposal_target", columnList = "target_entity_id"),
                @Index(name = "idx_proposal_volunteer", columnList = "volunteer_id"),
                @Index(name = "idx_proposal_status", columnList = "status")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_proposal_volunteer_target",
                        columnNames = {"volunteer_id", "target_entity_id", "type"}
                )
        }
)
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
     * - HELP_REQUEST
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProposalType type;

    /**
     * La entidad objetivo (HelpRequest o Donation)
     */
    @Column(name = "target_entity_id", nullable = false)
    private UUID targetEntityId;

    /**
     * Voluntario recomendado
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_id", nullable = false)
    private Volunteer volunteer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProposalStatus status;

    /**
     * Indica si la proposal está activa y visible para el voluntario.
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Puntuación o prioridad de recomendación que la IA le da a esta propuesta
     */
    private Double score;

    /**
     * Fecha en la que el voluntario respondió (aceptó o rechazó), nulo si no responde,
     */
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    /**
     * Para evitar Race Condition por si dos voluntarios acepten a la vez
     */
    @Version
    @Column(nullable = false)
    private Long version;

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
