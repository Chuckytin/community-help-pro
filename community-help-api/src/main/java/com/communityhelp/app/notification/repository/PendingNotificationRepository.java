package com.communityhelp.app.notification.repository;

import com.communityhelp.app.notification.model.PendingNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PendingNotificationRepository extends JpaRepository<PendingNotification, UUID> {
    List<PendingNotification> findBySentFalse();

    /**
     * Comprueba si ya existe notificación enviada para este voluntario y entidad
     */
    boolean existsByVolunteerIdAndEntityIdAndSentTrue(UUID volunteerId, UUID entityId);
}