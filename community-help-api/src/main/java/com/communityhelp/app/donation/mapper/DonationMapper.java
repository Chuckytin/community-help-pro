package com.communityhelp.app.donation.mapper;

import com.communityhelp.app.donation.dto.DonationResponseDto;
import com.communityhelp.app.donation.model.Donation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct crea un bean de Spring.
 * IGNORE - Se ignoran los campos que no se puedan mapear automáticamente.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DonationMapper {

    /**
     * Convierte Donation a DTO de respuesta
     * usa getLongitude()/getLatitude() de AuditableLocatable
     */
    @Mapping(target = "donorId", source = "donor.id")
    @Mapping(target = "volunteerId", source = "volunteer.id")
    @Mapping(target = "latitude", source = "latitude")
    @Mapping(target = "longitude", source = "longitude")
    DonationResponseDto toDto(Donation donation);

}
