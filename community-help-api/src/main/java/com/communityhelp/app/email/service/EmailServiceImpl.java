package com.communityhelp.app.email.service;

import com.communityhelp.app.notification.model.PendingNotification;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final Duration OTP_VERIFICATION_EXPIRATION = Duration.ofHours(24);
    private static final Duration OTP_RESET_EXPIRATION = Duration.ofMinutes(15);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    @Value("${app.frontend.url.login}")
    private String loginUrl;

    /**
     * Envía Email de verificación.
     */
    @Async
    @Override
    public void sendVerificationEmail(String to, String name, String otp) {
        Context ctx = new Context();
        ctx.setVariable("name", name);
        ctx.setVariable("otp", otp);
        ctx.setVariable("expirationTime", formatDuration(OTP_VERIFICATION_EXPIRATION));
        sendHtmlEmail(to, "Verifica tu email - Community Help", "verify-email", ctx);
    }

    /**
     * Envía Email de recuperación de contraseña.
     */
    @Async
    @Override
    public void sendPasswordResetEmail(String to, String name, String otp) {
        Context ctx = new Context();
        ctx.setVariable("name", name);
        ctx.setVariable("otp", otp);
        ctx.setVariable("expirationTime", formatDuration(OTP_RESET_EXPIRATION));
        sendHtmlEmail(to, "Recupera tu contraseña - Community Help", "password-reset-email", ctx);
    }

    /**
     * Envía Email de bienvenida al usuario.
     */
    @Async
    @Override
    public void sendWelcomeEmail(String to, String name) {
        Context ctx = new Context();
        ctx.setVariable("name", name);
        ctx.setVariable("loginUrl", loginUrl);
        sendHtmlEmail(to, "¡Bienvenido a Community Help!", "welcome-email", ctx);
    }

    /**
     * Envia Email notificando una nueva propuesta relacionada con el usuario.
     */
    @Async
    @Override
    public void sendProposalDigestEmail(String to, String name, List<PendingNotification> proposals) {
        Context ctx = new Context();
        ctx.setVariable("name", name);
        ctx.setVariable("proposals", proposals);
        ctx.setVariable("proposalCount", proposals.size());
        ctx.setVariable("loginUrl", loginUrl);
        sendHtmlEmail(to, "Tienes " + proposals.size() +
                " nueva" + (proposals.size() > 1 ? "s propuestas" : " propuesta") +
                " - Community Help", "proposal-digest-email", ctx);
    }

    /**
     * Envía un email HTML utilizando Thymeleaf para renderizar la plantilla con el contexto proporcionado.
     */
    private void sendHtmlEmail(String to, String subject, String template, Context ctx) {
        try {
            String html = templateEngine.process(template, ctx);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, "Community Help");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("[EmailService] Email '{}' sent to {}", subject, to);
        } catch (Exception e) {
            log.error("[EmailService] Error sending email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Formatea una duración en un formato legible (días, horas o minutos) para mostrar en los emails.
     */
    private String formatDuration(Duration duration) {
        if (duration.toHours() >= 24) {
            return duration.toDays() + " día";
        } else if (duration.toMinutes() >= 60) {
            return duration.toHours() + " horas";
        } else {
            return duration.toMinutes() + " minutos";
        }
    }
}