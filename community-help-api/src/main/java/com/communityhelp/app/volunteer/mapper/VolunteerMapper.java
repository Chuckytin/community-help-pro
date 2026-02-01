package com.communityhelp.app.volunteer.mapper;

import com.communityhelp.app.volunteer.dto.VolunteerResponseDto;
import com.communityhelp.app.volunteer.model.Volunteer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct crea una bean de Spring.
 * IGNORE - Se ignoran los campos que no se puedan mapear.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VolunteerMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "rating", source = "user.rating")
    @Mapping(target = "latitude", expression = "java(volunteer.getUser().getLatitude())")
    @Mapping(target = "longitude", expression = "java(volunteer.getUser().getLongitude())")
    VolunteerResponseDto toDto(Volunteer volunteer);

}
