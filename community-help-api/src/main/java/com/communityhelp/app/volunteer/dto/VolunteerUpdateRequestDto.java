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
 * Actualización que envía el usuario desde el frontend para modificar su Volunteer
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerUpdateRequestDto {

    private Boolean available;

    @Positive(message = "Radius must be greater than 0")
    private Double radiusKm;

    @Size(max = 20, message = "Too many skills selected")
    private Set<VolunteerSkill> skills;

}
