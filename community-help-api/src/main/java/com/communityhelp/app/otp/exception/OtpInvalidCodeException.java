package com.communityhelp.app.otp.exception;

import com.communityhelp.app.common.exceptions.ErrorCode;

public class OtpInvalidCodeException extends OtpException {
    public OtpInvalidCodeException() {
        super(ErrorCode.OTP_INVALID_CODE, "Invalid verification code. Please check and try again.");
    }
}