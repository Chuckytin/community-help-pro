package com.communityhelp.app.auth.mapper;

import com.communityhelp.app.auth.dto.AuthResponse;
import com.communityhelp.app.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct crea una bean de Spring.
 * IGNORE - Se ignoran los campos que no se puedan mapear.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthResponseMapper {

    /**
     * Convierte User + token a AuthResponse
     */
    @Mapping(target = "user", source = "user")
    @Mapping(target = "token", source = "jwtToken")
    @Mapping(target = "expiredIn", source = "expiration")
    AuthResponse toAuthResponse(User user, String jwtToken, long expiration);

}
