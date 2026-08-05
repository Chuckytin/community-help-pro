package com.communityhelp.app.otp.service;

import com.communityhelp.app.otp.exception.OtpAlreadyUsedException;
import com.communityhelp.app.otp.exception.OtpExpiredException;
import com.communityhelp.app.otp.exception.OtpInvalidCodeException;
import com.communityhelp.app.otp.exception.OtpNotFoundException;
import com.communityhelp.app.otp.model.OtpCode;
import com.communityhelp.app.otp.model.OtpType;
import com.communityhelp.app.otp.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Implementación del servicio de OTP (One-Time Password).
 * Gestiona la generación, persistencia y validación de códigos de un solo uso
 * para verificación de email y recuperación de contraseña.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;

    @Value("${otp.expiration-minutes:15}")
    private int expirationMinutes;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Genera un nuevo código OTP de 6 dígitos y lo persiste en base de datos.
     * - Elimina los OTPs anteriores del mismo tipo para ese email antes de crear uno nuevo.
     * - El código se genera con SecureRandom() para garantizar aleatoriedad criptográfica.
     */
    @Override
    public String generateAndSave(String email, OtpType type) {
        otpRepository.deleteAllByEmailAndType(email, type);

        String code = String.format("%06d", secureRandom.nextInt(1_000_000));

        OtpCode otp = OtpCode.builder()
                .email(email)
                .code(code)
                .type(type)
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                .used(false)
                .build();

        otpRepository.save(otp);

        return code;
    }

    /**
     * Valida un código OTP para el email y tipo indicados.
     * - Busca el OTP más reciente no usado para ese email y tipo.
     * - Comprueba que el código coincida y que no haya expirado.
     * - Verifica si hay algún OTP (usado o expirado) para dar mejor mensaje.
     * - Verifica si está expirado.
     * - Verifica si ya fue usado.
     * - Si no hay ningún OTP para este email/tipo.
     * - Valida que el código coincida.
     * - Valida expiración (doble verificación).
     * - Marca como usado.
     */
    @Override
    public boolean validate(String email, String code, OtpType type) {
        log.info("Validating OTP for email: {}, type: {}", email, type);

        OtpCode otp = otpRepository
                .findTopByEmailAndTypeOrderByExpiresAtDesc(email, type)
                .orElseThrow(() -> {
                    log.warn("No OTP found for email: {}, type: {}", email, type);

                    return new OtpNotFoundException();
                });

        if (otp.isUsed()) {
            log.warn("OTP already used for email: {}", email);
            throw new OtpAlreadyUsedException();
        }

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("OTP expired for email: {}, expiresAt: {}", email, otp.getExpiresAt());
            throw new OtpExpiredException();
        }

        if (!otp.getCode().equals(code)) {
            log.warn("Invalid OTP code for email: {}", email);
            throw new OtpInvalidCodeException();
        }

        otp.setUsed(true);
        otpRepository.save(otp);

        log.info("OTP validated successfully for email: {}", email);
        
        return true;
    }
}