package com.communityhelp.app.otp.exception;

import com.communityhelp.app.common.exceptions.ErrorCode;

public class OtpAlreadyUsedException extends OtpException {
    public OtpAlreadyUsedException() {
        super(ErrorCode.OTP_ALREADY_USED, "This verification code has already been used.");
    }
}