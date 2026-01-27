package com.communityhelp.app.donation.model;

import com.communityhelp.app.common.persistence.AuditableLocatable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "donations")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Donation extends AuditableLocatable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "donor_id", nullable = false)
    private UUID donorId;

    @Column(name = "volunteer_id")
    private UUID volunteerId;

    @Column(name = "donation_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private DonationType donationType;

    @Column(name = "food_type")
    @Enumerated(EnumType.STRING)
    private FoodType foodType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DonationStatus status;

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
