package com.communityhelp.app.common.exceptions;

public class DuplicateDonationException extends BusinessException {
    public DuplicateDonationException() {
        super(ErrorCode.DONATION_DUPLICATE_TITLE,
                "You already have an active donation with this title.");
    }
}
