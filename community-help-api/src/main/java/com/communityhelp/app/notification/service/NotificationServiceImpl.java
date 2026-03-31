package com.communityhelp.app.notification.service;

import com.communityhelp.app.email.service.EmailService;
import com.communityhelp.app.notification.model.PendingNotification;
import com.communityhelp.app.notification.repository.PendingNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final PendingNotificationRepository notificationRepository;
    private final EmailService emailService;

    /**
     * Encola una notificación pendiente para un voluntario.
     * No envía el email inmediatamente — el scheduler lo agrupará.
     */
    public void enqueueProposalNotification(UUID volunteerId, String email,
                                            String name, String entityTitle,
                                            String entityType) {
        notificationRepository.save(PendingNotification.builder()
                .volunteerId(volunteerId)
                .volunteerEmail(email)
                .volunteerName(name)
                .entityTitle(entityTitle)
                .entityType(entityType)
                .createdAt(LocalDateTime.now())
                .sent(false)
                .build());

        log.debug("[notification] Queued proposal notification for volunteer {}", volunteerId);
    }

    /**
     * Cada 5 minutos agrupa las notificaciones pendientes por voluntario
     * y envía un único email de resumen a cada uno.
     */
    @Scheduled(fixedDelayString = "${notification.digest.interval-ms}")
    public void sendDigests() {
        List<PendingNotification> pending = notificationRepository.findBySentFalse();

        if (pending.isEmpty()) return;

        // Agrupa por voluntario
        Map<UUID, List<PendingNotification>> byVolunteer = pending.stream()
                .collect(Collectors.groupingBy(PendingNotification::getVolunteerId));

        log.info("[notification-digest] Sending digests to {} volunteers ({} total notifications)",
                byVolunteer.size(), pending.size());

        byVolunteer.forEach((volunteerId, notifications) -> {
            PendingNotification first = notifications.getFirst();
            try {
                emailService.sendProposalDigestEmail(
                        first.getVolunteerEmail(),
                        first.getVolunteerName(),
                        notifications
                );
                // Marca como enviadas
                notifications.forEach(n -> n.setSent(true));
                notificationRepository.saveAll(notifications);

                log.info("[notification-digest] Digest sent to {} ({} proposals)",
                        first.getVolunteerEmail(), notifications.size());
            } catch (Exception e) {
                log.error("[notification-digest] Error sending digest to {}: {}",
                        first.getVolunteerEmail(), e.getMessage());
            }
        });
    }
}