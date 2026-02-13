package com.communityhelp.app.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Actualización que envía el usuario desde el frontend para modificar su Review
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewUpdateRequestDto {

    @Min(1)
    @Max(5)
    @NotNull
    private Integer rating;

    @Size(max = 2000)
    private String comment;

}
