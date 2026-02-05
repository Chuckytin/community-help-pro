package com.communityhelp.app.helprequest.dto;

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

    private String title;
    private String description;
    private LocalDateTime deadline;
    private Double latitude;
    private Double longitude;

}
