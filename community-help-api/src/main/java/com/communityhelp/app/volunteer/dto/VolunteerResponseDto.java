package com.communityhelp.app.volunteer.dto;

import com.communityhelp.app.volunteer.model.VolunteerSkill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * DTO que se enviará al frontend con información de User y Volunteer
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerResponseDto {

    private UUID userId;

    private String name;
    private String email;

    private boolean available;
    private Double radiusKm;
    private Set<VolunteerSkill> skills;

    private Double latitude;
    private Double longitude;
    private Double rating;

}
