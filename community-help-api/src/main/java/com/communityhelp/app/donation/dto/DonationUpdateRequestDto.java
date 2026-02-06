package com.communityhelp.app.donation.dto;

import com.communityhelp.app.donation.model.DonationType;
import com.communityhelp.app.donation.model.FoodType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Actualización que envía el usuario desde el frontend para modificar su Donation
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationUpdateRequestDto {

    @Size(max = 120)
    private String title;

    @Size(max = 1000)
    private String description;

    private DonationType donationType;
    private FoodType foodType;

    @Positive
    private Integer quantity;

    @Size(max = 30)
    private String unit;

    @Future
    private LocalDateTime expiryDate;

    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double latitude;

    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double longitude;

}
