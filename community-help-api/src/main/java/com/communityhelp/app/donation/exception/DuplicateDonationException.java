package com.communityhelp.app.donation.exception;

import com.communityhelp.app.common.exceptions.BusinessException;
import com.communityhelp.app.common.exceptions.ErrorCode;

public class DuplicateDonationException extends BusinessException {
    public DuplicateDonationException() {
        super(ErrorCode.DONATION_DUPLICATE_TITLE,
                "You already have an active donation with this title.");
    }
}
