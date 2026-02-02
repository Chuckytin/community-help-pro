package com.communityhelp.app.helprequest.dto;

import com.communityhelp.app.helprequest.model.HelpRequestType;
import lombok.*;

import java.time.LocalDateTime;

/**
 * La solicitud que envía el usuario desde el frontend para crear una HelpRequest
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpRequestCreateRequestDto {

    private HelpRequestType type;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private Double latitude;
    private Double longitude;

}
