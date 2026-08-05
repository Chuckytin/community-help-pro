package com.communityhelp.app.auth.service;

import com.communityhelp.app.auth.dto.AuthResponse;
import com.communityhelp.app.auth.dto.ResetPasswordRequestDto;
import com.communityhelp.app.auth.dto.VerifyEmailRequestDto;
import com.communityhelp.app.email.service.EmailService;
import com.communityhelp.app.otp.exception.OtpException;
import com.communityhelp.app.otp.model.OtpType;
import com.communityhelp.app.otp.repository.OtpRepository;
import com.communityhelp.app.otp.service.OtpService;
import com.communityhelp.app.user.dto.LoginRequestDto;
import com.communityhelp.app.user.dto.UserCreateRequestDto;
import com.communityhelp.app.user.dto.UserResponseDto;
import com.communityhelp.app.user.exception.DuplicateEmailException;
import com.communityhelp.app.user.exception.EmailNotVerifiedException;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final JwtService jwtService;
    private final UserService userService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final TokenBlacklistService tokenBlacklistService;

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
            userDetails = jwtService.authenticate(dto.getEmail(), dto.getPassword());
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(userDetails);

        UserResponseDto userResponseDto = userService.getUserByEmail(dto.getEmail());

        if (!userResponseDto.isEmailVerified()) {
            throw new EmailNotVerifiedException("Email not verified");
        }

        return AuthResponse.builder()
                .token(token)
                .expiredIn(jwtService.getJwtExpiryMs())
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
                throw new DuplicateEmailException();
            }
        } else {
            createdUser = userService.createUser(dto);
        }

        UserDetails userDetails = jwtService.authenticate(dto.getEmail(), dto.getPassword());
        String token = jwtService.generateToken(userDetails);

        String otp = otpService.generateAndSave(createdUser.getEmail(), OtpType.VERIFY_EMAIL);
        emailService.sendVerificationEmail(createdUser.getEmail(), createdUser.getName(), otp);

        return AuthResponse.builder()
                .token(token)
                .expiredIn(jwtService.getJwtExpiryMs())
                .user(createdUser)
                .build();
    }

    /**
     * Verifica el email del usuario con el código OTP.
     * Marca email como verificado.
     * Envia el email de bienvenida.
     */
    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequestDto dto) {
        log.info("Verifying email for: {}", dto.getEmail());

        try {
            otpService.validate(dto.getEmail(), dto.getCode(), OtpType.VERIFY_EMAIL);

            userService.markEmailVerified(dto.getEmail());
            log.info("Email verified successfully for: {}", dto.getEmail());

            UserResponseDto user = userService.getUserByEmail(dto.getEmail());
            emailService.sendWelcomeEmail(user.getEmail(), user.getName());

        } catch (OtpException e) {
            log.warn("OTP validation failed for {}: {}", dto.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error verifying email for {}: {}", dto.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Error verifying email. Please try again later.");
        }
    }

    /**
     * Inicia el proceso de recuperación de contraseña.
     * - Genera un código OTP para restablecer la contraseña y lo guarda asociado al email del usuario.
     * - Envía un email al usuario con el código OTP para restable
     */
    @Override
    public void forgotPassword(String email) {
        try {
            UserResponseDto user = userService.getUserByEmail(email);
            String code = otpService.generateAndSave(email, OtpType.RESET_PASSWORD);
            emailService.sendPasswordResetEmail(email, user.getName(), code);
        } catch (EntityNotFoundException e) {
            log.warn("Password reset requested for non-existent email: {}", email);
        }
    }

    /**
     * Restablece la contraseña del usuario.
     * - Valida el código OTP para restablecer la contraseña.
     * - Si el código es válido, actualiza la contraseña del usuario.
     */
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequestDto dto) {
        log.info("Resetting password for email: {}", dto.getEmail());

        try {
            otpService.validate(dto.getEmail(), dto.getCode(), OtpType.RESET_PASSWORD);

            userService.updatePassword(dto.getEmail(), dto.getNewPassword());
            log.info("Password reset successfully for: {}", dto.getEmail());

        } catch (OtpException e) {
            log.warn("OTP validation failed for password reset {}: {}", dto.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error resetting password for {}: {}", dto.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Error resetting password. Please try again later.");
        }
    }

    /**
     * Invalida el token actual añadiéndolo a la blacklist hasta su expiración natural.
     */
    @Override
    public void logout(String token) {
        Instant expiresAt = jwtService.getExpiration(token);
        tokenBlacklistService.blacklist(token, expiresAt);
        log.info("[logout] Token blacklisted, expires at {}", expiresAt);
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

        List<String> emails = unverified.stream()
                .map(User::getEmail)
                .toList();

        otpRepository.deleteByEmailIn(emails);

        userRepository.deleteAll(unverified);

        log.info("[cleanup] Deleted {} unverified users older than 24h", unverified.size());
    }

}
