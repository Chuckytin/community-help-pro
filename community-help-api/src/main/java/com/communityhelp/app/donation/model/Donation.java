package com.communityhelp.app.donation.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

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
public class Donation {

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

    @Column(name = "location", columnDefinition = "geography(POINT,4326)")
    @JdbcTypeCode(SqlTypes.GEOGRAPHY)
    private Point location;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

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

    // Helper latitude (no se guarda en BD)
    @Transient
    public Double getLatitude() {
        return location != null ? location.getY() : null;
    }

    // Helper longitude (no se guarda en BD)
    @Transient
    public Double getLongitude() {
        return location != null ? location.getX() : null;
    }

    // Método cómodo para setear desde frontend (latitude, longitude)
    public void setLocation(double latitude, double longitude) {
        this.location = new GeometryFactory().createPoint(new Coordinate(longitude, latitude));
        this.location.setSRID(4326);
    }

}
