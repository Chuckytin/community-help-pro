package com.communityhelp.app.auth.service;

import com.communityhelp.app.auth.dto.AuthResponse;
import com.communityhelp.app.user.dto.LoginRequestDto;
import com.communityhelp.app.user.dto.UserCreateRequestDto;
import com.communityhelp.app.user.dto.UserResponseDto;
import com.communityhelp.app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    /**
     * Autentica al usuario con email y contraseña.
     * - Si las credenciales son válidas, genera un token JWT.
     * - Devuelve un {@link AuthResponse} que incluye el token, el tiempo de expiración y los datos del usuario.
     */
    @Override
    public AuthResponse login(LoginRequestDto dto) {
        UserDetails userDetails;

        try {
            userDetails = authenticationService.authenticate(dto.getEmail(), dto.getPassword());
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = authenticationService.generateToken(userDetails);

        UserResponseDto userResponseDto = userService.getUserByEmail(dto.getEmail());

        // Construcción del AuthResponse
        return AuthResponse.builder()
                .token(token)
                .expiredIn(authenticationService.getJwtExpiryMs())
                .user(userResponseDto)
                .build();
    }

    /**
     * Registra al usuario con email y contraseña.
     * - Crea el usuario.
     * - Lo autentica automáticamente.
     * - Devuelve token + datos del usuario.
     */
    @Override
    @Transactional
    public AuthResponse register(UserCreateRequestDto dto) {
        UserResponseDto createdUser = userService.createUser(dto);

        UserDetails userDetails =
                authenticationService.authenticate(dto.getEmail(), dto.getPassword());

        String token = authenticationService.generateToken(userDetails);

        // Construcción del AuthResponse
        return AuthResponse.builder()
                .token(token)
                .expiredIn(authenticationService.getJwtExpiryMs())
                .user(createdUser)
                .build();

    }

}
