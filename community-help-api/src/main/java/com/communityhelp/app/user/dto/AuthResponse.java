package com.communityhelp.app.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta tras autenticarse un usuario.
 * Contiene el token JWT y opcionalmente los datos del usuario autenticado.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private long expiredIn;

    private UserResponseDto user;

}
