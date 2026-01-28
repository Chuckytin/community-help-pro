package com.communityhelp.app.auth.dto;

import com.communityhelp.app.user.dto.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta que recibirá el frontend tras autenticarse un usuario.
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
