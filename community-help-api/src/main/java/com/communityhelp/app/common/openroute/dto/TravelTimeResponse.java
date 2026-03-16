package com.communityhelp.app.common.openroute.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa una tiempo de viaje enviado al frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelTimeResponse {

    private double distance;
    private double duration;

}
