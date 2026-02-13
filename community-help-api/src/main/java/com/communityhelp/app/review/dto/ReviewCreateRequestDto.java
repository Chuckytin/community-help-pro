package com.communityhelp.app.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * La Review que envía el usuario desde el frontend para valorar la HelpRequest/Donation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateRequestDto {

    // authorId saldrá del JWT con el usuario logueado (seguridad)

    @NotNull
    private UUID targetId;

    private UUID donationId;

    private UUID helpRequestId;

    @Min(1)
    @Max(5)
    @NotNull
    private Integer rating;

    @Size(max = 2000)
    private String comment;

}
