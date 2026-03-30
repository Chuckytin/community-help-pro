package com.communityhelp.app.common.openroute.dto;

import com.communityhelp.app.common.openroute.model.TransportMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FastestTravelResponse {
    private double distance;
    private double duration;
    private TransportMode fastestMode;
}
