package com.communityhelp.app.helprequest.model;

import com.communityhelp.app.common.persistence.AuditableLocatable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "help_requests")
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
     * ID del usuario que hace la solicitud
     */
    @Column(name = "requester_id", nullable = false)
    private UUID requesterId;

    /**
     * ID del voluntario asignado (si corresponde)
     */
    @Column(name = "volunteer_id")
    private UUID volunteerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private HelpRequestType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private HelpRequestStatus status;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

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
