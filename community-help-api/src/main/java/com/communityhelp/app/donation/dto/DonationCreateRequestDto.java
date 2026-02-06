package com.communityhelp.app.donation.dto;

import com.communityhelp.app.donation.model.DonationType;
import com.communityhelp.app.donation.model.FoodType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * La solicitud que envía el usuario desde el frontend para crear una Donation
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationCreateRequestDto {

    @NotNull
    private DonationType donationType;

    // opcional, solo si donationType == FOOD
    private FoodType foodType;

    @NotBlank
    @Size(max = 120)
    private String title;

    @NotBlank
    @Size(max = 1000)
    private String description;

    @NotNull
    @Positive
    private Integer quantity;

    @NotBlank
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
