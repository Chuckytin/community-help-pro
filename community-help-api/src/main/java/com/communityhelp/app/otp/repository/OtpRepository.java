package com.communityhelp.app.otp.repository;

import com.communityhelp.app.otp.model.OtpCode;
import com.communityhelp.app.otp.model.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
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

    /**
     * Elimina todos los OTPs para una lista de emails. Esto se puede usar para limpiar OTPs antiguos o no utilizados
     */
    void deleteByEmailIn(List<String> emails);
}