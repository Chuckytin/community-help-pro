package com.communityhelp.app.common.exceptions;

public class DuplicateHelpRequestException extends BusinessException {
    public DuplicateHelpRequestException() {
        super(ErrorCode.HELP_REQUEST_DUPLICATE_TITLE,
                "You already have an active help request with this title.");
    }
}
