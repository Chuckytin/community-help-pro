package com.communityhelp.app.otp.service;

import com.communityhelp.app.otp.model.OtpType;

public interface OtpService {
    String generateAndSave(String email, OtpType type);
    boolean validate(String email, String code, OtpType type);
}