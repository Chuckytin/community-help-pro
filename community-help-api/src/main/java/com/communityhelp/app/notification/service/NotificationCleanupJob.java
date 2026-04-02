package com.communityhelp.app.notification.service;

import com.communityhelp.app.notification.repository.PendingNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationCleanupJob {

    private final PendingNotificationRepository notificationRepository;

    @Value("${notification.cleanup.retention-days:30}")
    private int retentionDays;

    /**
     * Cada día a las 3:30am borra las notificaciones marcadas como enviadas
     * que sean más antiguas que el período de retención configurado.
     */
    @Scheduled(cron = "0 30 3 * * *")
    @Transactional
    public void cleanupSentNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);

        int deleted = notificationRepository.deleteBysentTrueAndCreatedAtBefore(cutoff);

        if (deleted > 0) {
            log.info("[notification-cleanup] Deleted {} sent notifications older than {}d",
                    deleted, retentionDays);
        } else {
            log.debug("[notification-cleanup] Nothing to clean");
        }
    }
}
