package com.communityhelp.app.helprequest.dto;

import com.communityhelp.app.helprequest.model.HelpRequestStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Actualización que envía el usuario desde el frontend para modificar su HelpRequest
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpRequestUpdateRequestDto {

    private String title;
    private String description;
    private LocalDateTime deadline;
    private Double latitude;
    private Double longitude;

}
