package com.communityhelp.app.otp.repository;

import com.communityhelp.app.otp.entity.OtpCode;
import com.communityhelp.app.otp.entity.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OtpRepository extends JpaRepository<OtpCode, Long> {

    /**
     * Busca el OTP más reciente no usado para un email y tipo de dados.
     */
    Optional<OtpCode> findTopByEmailAndTypeAndUsedFalseOrderByExpiresAtDesc(String email, OtpType type);

    /**
     * Elimina todos los OTPs para un email y tipo dados. Esto se utiliza antes de generar un nuevo OTP
     * para asegurarse de que solo haya un OTP activo por email y tipo.
     */
    void deleteAllByEmailAndType(String email, OtpType type);
}