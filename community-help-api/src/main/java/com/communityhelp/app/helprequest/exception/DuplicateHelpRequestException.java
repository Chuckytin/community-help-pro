package com.communityhelp.app.helprequest.exception;

import com.communityhelp.app.common.exceptions.BusinessException;
import com.communityhelp.app.common.exceptions.ErrorCode;

public class DuplicateHelpRequestException extends BusinessException {
    public DuplicateHelpRequestException() {
        super(ErrorCode.HELP_REQUEST_DUPLICATE_TITLE,
                "You already have an active help request with this title.");
    }
}
