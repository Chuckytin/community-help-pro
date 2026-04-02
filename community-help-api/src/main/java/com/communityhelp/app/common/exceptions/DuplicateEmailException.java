package com.communityhelp.app.common.exceptions;

public class DuplicateEmailException extends BusinessException {
    public DuplicateEmailException() {
        super(ErrorCode.EMAIL_ALREADY_EXISTS,
                "This email is already registered.");
    }
}
