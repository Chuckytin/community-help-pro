package com.communityhelp.app.otp.exception;

import com.communityhelp.app.common.exceptions.ErrorCode;

public class OtpExpiredException extends OtpException {
    public OtpExpiredException() {
        super(ErrorCode.OTP_EXPIRED, "The verification code has expired. Please request a new one.");
    }
}