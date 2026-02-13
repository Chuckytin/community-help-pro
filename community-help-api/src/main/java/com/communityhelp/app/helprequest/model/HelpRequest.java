package com.communityhelp.app.helprequest.model;

import com.communityhelp.app.common.persistence.AuditableLocatable;
import com.communityhelp.app.user.model.User;
import com.communityhelp.app.volunteer.model.Volunteer;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "help_requests",
        indexes = {
                @Index(name = "idx_help_status", columnList = "status"),
                @Index(name = "idx_help_requester", columnList = "requester_id"),
                @Index(name = "idx_help_volunteer", columnList = "volunteer_id"),
                @Index(name = "idx_help_requester_status", columnList = "requester_id, status")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class HelpRequest extends AuditableLocatable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Usuario que solicita la ayuda
     * LAZY - evita cargar el User completo al menos que se acceda explícitamente a él.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    /**
     * Voluntario que acepta la solicitud
     * LAZY - evita cargar el Volunteer completo al menos que se acceda explícitamente a él.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_id")
    private Volunteer volunteer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HelpRequestType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HelpRequestStatus status;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Helper del User solicitante que devuelve su id.
     * Transient para que no forme parte del estado persistente de la entidad
     * (vive solo en memoria, no en la BBDD)
     */
    @Transient
    public UUID getRequesterId() {
        return requester != null ? requester.getId() : null;
    }

    /**
     * Helper del User voluntario que devuelve su id.
     * Transient para que no forme parte del estado persistente de la entidad
     * (vive solo en memoria, no en la BBDD)
     */
    @Transient
    public UUID getVolunteerId() {
        return volunteer != null ? volunteer.getId() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HelpRequest that = (HelpRequest) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
