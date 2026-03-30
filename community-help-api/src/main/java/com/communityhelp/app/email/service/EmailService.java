package com.communityhelp.app.email.service;

public interface EmailService {
    void sendVerificationEmail(String to, String name, String otp);
    void sendPasswordResetEmail(String to, String name, String otp);
    void sendWelcomeEmail(String to, String name);
}