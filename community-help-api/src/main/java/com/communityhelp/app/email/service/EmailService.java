package com.communityhelp.app.email.service;

import com.communityhelp.app.notification.model.PendingNotification;

import java.util.List;

public interface EmailService {
    void sendVerificationEmail(String to, String name, String otp);
    void sendPasswordResetEmail(String to, String name, String otp);
    void sendWelcomeEmail(String to, String name);
    void sendProposalDigestEmail(String to, String name, List<PendingNotification> proposals);
}