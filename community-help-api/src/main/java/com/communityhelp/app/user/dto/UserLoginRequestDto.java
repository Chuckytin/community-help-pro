package com.communityhelp.app.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lo que necesita el User para el Login
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRequestDto {

    private String email;
    private String password;

}
