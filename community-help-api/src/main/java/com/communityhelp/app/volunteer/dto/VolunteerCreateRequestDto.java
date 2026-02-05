package com.communityhelp.app.volunteer.dto;

import com.communityhelp.app.volunteer.model.VolunteerSkill;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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

    @Positive(message = "Radius must be greater than 0")
    private Double radiusKm;

    @Size(max = 20, message = "Too many skills selected")
    private Set<VolunteerSkill> skills;

}
