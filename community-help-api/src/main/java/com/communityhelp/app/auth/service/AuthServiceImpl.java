package com.communityhelp.app.auth.service;

import com.communityhelp.app.auth.dto.AuthResponse;
import com.communityhelp.app.auth.dto.ResetPasswordRequestDto;
import com.communityhelp.app.auth.dto.VerifyEmailRequestDto;
import com.communityhelp.app.common.exceptions.BusinessException;
import com.communityhelp.app.common.exceptions.EmailNotVerifiedException;
import com.communityhelp.app.common.exceptions.ErrorCode;
import com.communityhelp.app.email.service.EmailService;
import com.communityhelp.app.otp.entity.OtpType;
import com.communityhelp.app.otp.repository.OtpRepository;
import com.communityhelp.app.otp.service.OtpService;
import com.communityhelp.app.user.dto.LoginRequestDto;
import com.communityhelp.app.user.dto.UserCreateRequestDto;
import com.communityhelp.app.user.dto.UserResponseDto;
import com.communityhelp.app.user.model.User;
import com.communityhelp.app.user.repository.UserRepository;
import com.communityhelp.app.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final OtpRepository otpRepository;

    /**
     * Autentica al usuario con email y contraseña.
     * - Si las credenciales son válidas, genera un token JWT.
     * - Verifica que el email del usuario esté verificado, si no lo está lanza una excepción.
     * - Devuelve el token JWT y los datos del usuario en la respuesta.
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

        if (!userResponseDto.isEmailVerified()) {
            throw new EmailNotVerifiedException("Email not verified");
        }

        return AuthResponse.builder()
                .token(token)
                .expiredIn(authenticationService.getJwtExpiryMs())
                .user(userResponseDto)
                .build();
    }

    /**
     * Registra al usuario con email y contraseña.
     * - Busca al usuario existente, incluyendo inactivo, si no existe continúa para crear la cuenta.
     * - Si existe el user y está inactivo, reactiva la cuenta, si no existe crea la cuenta.
     * - Autentica al usuario para generar el token JWT.
     * - Genera un OTP de verificación de email y lo envía al usuario.
     */
    @Override
    public AuthResponse register(UserCreateRequestDto dto) {

        UserResponseDto createdUser;

        UserResponseDto existingUser = null;
        try {
            existingUser = userService.getUserByEmailIncludeInactive(dto.getEmail());
        } catch (EntityNotFoundException ignored) {

        }

        if (existingUser != null) {
            if (!existingUser.isActive()) {
                createdUser = userService.reactivateUser(existingUser.getId(), dto);
            } else {
                throw new BusinessException(
                        ErrorCode.EMAIL_ALREADY_EXISTS,
                        "This email is already registered."
                );
            }
        } else {
            createdUser = userService.createUser(dto);
        }

        UserDetails userDetails = authenticationService.authenticate(dto.getEmail(), dto.getPassword());
        String token = authenticationService.generateToken(userDetails);

        String otp = otpService.generateAndSave(createdUser.getEmail(), OtpType.VERIFY_EMAIL);
        emailService.sendVerificationEmail(createdUser.getEmail(), createdUser.getName(), otp);

        return AuthResponse.builder()
                .token(token)
                .expiredIn(authenticationService.getJwtExpiryMs())
                .user(createdUser)
                .build();
    }

    /**
     * Verifica el email del usuario con el código OTP.
     */
    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequestDto dto) {
        boolean valid = otpService.validate(dto.getEmail(), dto.getCode(), OtpType.VERIFY_EMAIL);
        if (!valid) {
            throw new BadCredentialsException("Invalid or expired verification code");
        }
        userService.markEmailVerified(dto.getEmail());

        UserResponseDto user = userService.getUserByEmail(dto.getEmail());
        emailService.sendWelcomeEmail(user.getEmail(), user.getName());
    }

    /**
     * Inicia el proceso de recuperación de contraseña.
     * - Genera un código OTP para restablecer la contraseña y lo guarda asociado al email del usuario.
     * - Envía un email al usuario con el código OTP para restable
     */
    @Override
    public void forgotPassword(String email) {
        UserResponseDto user = userService.getUserByEmail(email);
        String code = otpService.generateAndSave(email, OtpType.RESET_PASSWORD);
        emailService.sendPasswordResetEmail(email, user.getName(), code);
    }

    /**
     * Restablece la contraseña del usuario.
     * - Valida el código OTP para restablecer la contraseña.
     * - Si el código es válido, actualiza la contraseña del usuario.
     */
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequestDto dto) {
        boolean valid = otpService.validate(dto.getEmail(), dto.getCode(), OtpType.RESET_PASSWORD);
        if (!valid) {
            throw new BadCredentialsException("Invalid or expired reset code");
        }
        userService.updatePassword(dto.getEmail(), dto.getNewPassword());
    }

    /**
     * Caducidad de usuarios no verificados:
     * - Se ejecuta diariamente a las 3 AM.
     * - Busca usuarios con email no verificado y fecha de creación anterior a 24 horas.
     * - Elimina esos usuarios y los OTPs asociados para liberar recursos y mantener la base
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupUnverifiedUsers() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);

        List<User> unverified = userRepository
                .findByEmailVerifiedFalseAndCreatedAtBefore(cutoff);

        if (unverified.isEmpty()) {
            log.debug("[cleanup] No unverified users to delete");
            return;
        }

        List<UUID> ids = unverified.stream().map(User::getId).toList();

        // Elimina OTPs asociados primero
        otpRepository.deleteByEmailIn(
                unverified.stream().map(User::getEmail).toList()
        );

        userRepository.deleteAll(unverified);
        log.info("[cleanup] Deleted {} unverified users older than 24h", unverified.size());
    }

}
