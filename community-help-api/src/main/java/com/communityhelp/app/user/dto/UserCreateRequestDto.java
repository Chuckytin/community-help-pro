package com.communityhelp.app.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lo que recibe el usuario del frontend al registrarse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequestDto {

    private String name;
    private String email;
    private String password; // passwordHash en el Service

}
