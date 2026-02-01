package com.communityhelp.app.volunteer.dto;

import com.communityhelp.app.volunteer.model.VolunteerSkill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * La solicitud que envía el usuario desde el frontend para convertirse en Volunteer
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerCreateRequestDto {

    private Boolean available;
    private Double radiusKm;
    private Set<VolunteerSkill> skills;

}
