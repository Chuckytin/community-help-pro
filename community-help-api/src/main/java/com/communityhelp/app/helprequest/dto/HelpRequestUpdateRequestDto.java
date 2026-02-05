package com.communityhelp.app.helprequest.dto;

import com.communityhelp.app.helprequest.model.HelpRequestType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Actualización que envía el usuario desde el frontend para modificar su HelpRequest
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpRequestUpdateRequestDto {

    private HelpRequestType type;

    @Size(max = 120)
    private String title;

    @Size(max = 1000)
    private String description;

    @Future(message = "Deadline must be in the future")
    private LocalDateTime deadline;

    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double latitude;

    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double longitude;

}
