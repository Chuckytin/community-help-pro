package com.communityhelp.app.donation.model;

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
        name = "donations",
        indexes = {
                @Index(name = "idx_donation_status", columnList = "status"),
                @Index(name = "idx_donation_donor", columnList = "donor_id"),
                @Index(name = "idx_donation_volunteer", columnList = "volunteer_id"),
                @Index(name = "idx_donation_donor_status", columnList = "donor_id, status")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Donation extends AuditableLocatable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Usuario que crea la Donation
     * LAZY - evita cargar el User completo al menos que se acceda explícitamente a él.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;

    /**
     * Voluntario que distribuye la solicitud
     * LAZY - evita cargar el Volunteer completo al menos que se acceda explícitamente a él.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_id")
    private Volunteer volunteer;

    @Column(name = "donation_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private DonationType donationType;

    @Column(name = "food_type")
    @Enumerated(EnumType.STRING)
    private FoodType foodType;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DonationStatus status;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 30)
    private String unit;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Helper del User donante que devuelve su id.
     * Transient para que no forme parte del estado persistente de la entidad
     * (vive solo en memoria, no en la BBDD)
     */
    @Transient
    public UUID getDonorId() {
        return donor != null ? donor.getId() : null;
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
        Donation donation = (Donation) o;
        return Objects.equals(id, donation.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
