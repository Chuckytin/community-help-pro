package com.communityhelp.app.user.mapper;

import com.communityhelp.app.user.dto.UserCreateRequestDto;
import com.communityhelp.app.user.dto.UserResponseDto;
import com.communityhelp.app.user.dto.UserUpdateRequestDto;
import com.communityhelp.app.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct crea una bean de Spring.
 * IGNORE - Se ignoran los campos que no se puedan mapear.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    /**
     * Convierte User a un DTO de respuesta
     */
    @Mapping(target = "latitude", expression = "java(user.getLatitude())")
    @Mapping(target = "longitude", expression = "java(user.getLongitude())")
    UserResponseDto toDto(User user);

    /**
     * Convierte UserCreateRequestDto a entidad User
     * - El password aún no está hasheado
     */
    User toEntity(UserCreateRequestDto dto);

    /**
     * Actualiza un User con datos de UserUpdateRequestDto
     * - útil para PATH/PUT
     */
    //void updateFromDto(UserUpdateRequestDto dto, User user);

}
