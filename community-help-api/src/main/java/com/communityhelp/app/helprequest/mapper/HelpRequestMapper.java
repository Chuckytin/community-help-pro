package com.communityhelp.app.helprequest.mapper;

import com.communityhelp.app.helprequest.dto.HelpRequestResponseDto;
import com.communityhelp.app.helprequest.model.HelpRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct crea un bean de Spring.
 * IGNORE - Se ignoran los campos que no se puedan mapear automáticamente.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HelpRequestMapper {

    /**
     * Convierte HelpRequest a DTO de respuesta
     * usa getLongitude()/getLatitude() de AuditableLocatable
     */
    @Mapping(target = "requesterId", source = "requester.id")
    @Mapping(target = "volunteerId", source = "volunteer.id")
    //@Mapping(target = "latitude", source = "latitude")
    //@Mapping(target = "longitude", source = "longitude")
    HelpRequestResponseDto toDto(HelpRequest helpRequest);

}
