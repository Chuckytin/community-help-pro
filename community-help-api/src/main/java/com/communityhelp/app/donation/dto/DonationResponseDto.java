package com.communityhelp.app.donation.dto;

import com.communityhelp.app.donation.model.DonationStatus;
import com.communityhelp.app.donation.model.DonationType;
import com.communityhelp.app.donation.model.FoodType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta que se enviará al frontend con información de la Donation
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationResponseDto {

    private UUID id;
    private UUID donorId;
    private UUID volunteerId;
    private DonationType donationType;
    private FoodType foodType;
    private String title;
    private String description;
    private Integer quantity;
    private String unit;
    private Double latitude;
    private Double longitude;
    private DonationStatus status;
    private String cancelReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiryDate;
    private LocalDateTime pickedUpAt;

}
