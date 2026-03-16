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
     * Activo si la entidad participa en el motor de matchmaking
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

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

    /**
     * Asigna un voluntario a la solicitud.
     */
    public void assignVolunteer(Volunteer volunteer) {
        this.volunteer = volunteer;
        this.status = HelpRequestStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
    }

    /**
     * Libera al voluntario asignado.
     */
    public void releaseVolunteer() {
        this.volunteer = null;
        this.status = HelpRequestStatus.OPEN;
        this.acceptedAt = null;
    }

    /**
     * Completa una HelpRequest
     */
    public void complete() {
        this.status = HelpRequestStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.active = false;
    }

    /**
     * Cancela la solicitud y la excluye del motor de matching.
     */
    public void cancel(String reason) {
        this.status = HelpRequestStatus.CANCELLED;
        this.cancelReason = reason;
        this.active = false;
    }

    /**
     * Expira la fecha de deadline.
     */
    public void expire() {
        this.status = HelpRequestStatus.EXPIRED;
        this.active = false;
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
