package com.communityhelp.app.auth.service;

import com.communityhelp.app.auth.dto.AuthResponse;
import com.communityhelp.app.user.dto.LoginRequestDto;
import com.communityhelp.app.user.dto.UserCreateRequestDto;
import com.communityhelp.app.user.dto.UserResponseDto;
import com.communityhelp.app.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
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
    public AuthResponse register(UserCreateRequestDto dto) {

        UserResponseDto createdUser;

        // Busca el usuario existente, incluyendo inactivo
        UserResponseDto existingUser = null;
        try {
            existingUser = userService.getUserByEmailIncludeInactive(dto.getEmail());
        } catch (EntityNotFoundException ignored) {
            // Si no existe, continua para crear cuenta
        }

        // Si existe y está inactivo, reactiva la cuenta
        if (existingUser != null) {
            if (!existingUser.isActive()) {
                createdUser = userService.reactivateUser(existingUser.getId(), dto.getPassword());
            } else {
                throw new IllegalArgumentException("Email already in use");
            }
        } else {
            // Si no existe, crea la cuenta
            createdUser = userService.createUser(dto);
        }

        // Autenticación + token
        UserDetails userDetails = authenticationService.authenticate(dto.getEmail(), dto.getPassword());
        String token = authenticationService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .expiredIn(authenticationService.getJwtExpiryMs())
                .user(createdUser)
                .build();
    }

}
