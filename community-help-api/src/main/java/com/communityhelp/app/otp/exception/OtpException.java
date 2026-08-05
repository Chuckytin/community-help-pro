package com.communityhelp.app.otp.exception;

import com.communityhelp.app.common.exceptions.ErrorCode;
import lombok.Getter;

/**
 * Excepción específica para errores relacionados con OTP (códigos de verificación)
 */
@Getter
public class OtpException extends RuntimeException {

    private final ErrorCode errorCode;

    public OtpException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}