package com.communityhelp.app.otp.exception;

import com.communityhelp.app.common.exceptions.ErrorCode;

public class OtpNotFoundException extends OtpException {
    public OtpNotFoundException() {
        super(ErrorCode.OTP_NOT_FOUND, "No verification code found for this email. Please request a new one.");
    }
}