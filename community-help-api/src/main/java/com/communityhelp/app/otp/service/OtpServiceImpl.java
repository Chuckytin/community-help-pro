package com.communityhelp.app.otp.service;

import com.communityhelp.app.otp.entity.OtpCode;
import com.communityhelp.app.otp.entity.OtpType;
import com.communityhelp.app.otp.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
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
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;

    @Value("${otp.expiration-minutes:15}")
    private int expirationMinutes;

    /**
     * Genera un nuevo código OTP de 6 dígitos y lo persiste en base de datos.
     * - Elimina los OTPs anteriores del mismo tipo para ese email antes de crear uno nuevo.
     * - El código se genera con SecureRandom() para garantizar aleatoriedad criptográfica.
     */
    @Override
    public String generateAndSave(String email, OtpType type) {
        otpRepository.deleteAllByEmailAndType(email, type);

        String code = String.format("%06d", new SecureRandom().nextInt(999999));

        otpRepository.save(OtpCode.builder()
                .email(email)
                .code(code)
                .type(type)
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                .used(false)
                .build());

        return code;
    }

    /**
     * Valida un código OTP para el email y tipo indicados.
     * - Busca el OTP más reciente no usado para ese email y tipo.
     * - Comprueba que el código coincida y que no haya expirado.
     * - Si es válido, marca el OTP como usado para que no pueda reutilizarse.
     */
    @Override
    public boolean validate(String email, String code, OtpType type) {
        return otpRepository
                .findTopByEmailAndTypeAndUsedFalseOrderByExpiresAtDesc(email, type)
                .filter(otp -> otp.getCode().equals(code))
                .filter(otp -> otp.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(otp -> {
                    otp.setUsed(true);
                    otpRepository.save(otp);
                    return true;
                })
                .orElse(false);
    }
}