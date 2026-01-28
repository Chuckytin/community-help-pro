package com.communityhelp.app.auth.service;

import com.communityhelp.app.auth.dto.AuthResponse;
import com.communityhelp.app.user.dto.UserLoginRequestDto;
import com.communityhelp.app.user.mapper.UserMapper;
import com.communityhelp.app.user.model.User;
import com.communityhelp.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationService authenticationService;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    /**
     * Autentica al usuario con email y contraseña.
     * - Si las credenciales son válidas, genera un token JWT.
     * - Devuelve un {@link AuthResponse} que incluye el token y los datos del usuario.
     */
    @Override
    public AuthResponse login(UserLoginRequestDto dto) {
        UserDetails userDetails;

        try {
            userDetails = authenticationService.authenticate(dto.getEmail(), dto.getPassword());
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = authenticationService.generateToken(userDetails);

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        // Construcción del AuthResponse
        return AuthResponse.builder()
                .token(token)
                .expiredIn(authenticationService.getJwtExpiryMs())
                .user(userMapper.toDto(user))
                .build();
    }

}
