package com.communityhelp.app.volunteer.dto;

import com.communityhelp.app.volunteer.model.VolunteerSkill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Actualización que envía el usuario desde el frontend para modificar su Volunteer
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerUpdateRequestDto {

    private Boolean available;
    private Double radiusKm;
    private Set<VolunteerSkill> skills;

}
