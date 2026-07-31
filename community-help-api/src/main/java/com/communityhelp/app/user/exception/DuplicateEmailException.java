package com.communityhelp.app.user.exception;

import com.communityhelp.app.common.exceptions.BusinessException;
import com.communityhelp.app.common.exceptions.ErrorCode;

public class DuplicateEmailException extends BusinessException {
    public DuplicateEmailException() {
        super(ErrorCode.EMAIL_ALREADY_EXISTS,
                "This email is already registered.");
    }
}
