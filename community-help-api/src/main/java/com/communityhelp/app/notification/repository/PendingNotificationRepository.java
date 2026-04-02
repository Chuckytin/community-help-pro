package com.communityhelp.app.notification.repository;

import com.communityhelp.app.notification.model.PendingNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PendingNotificationRepository extends JpaRepository<PendingNotification, UUID> {
    List<PendingNotification> findBySentFalse();

    /**
     * Comprueba si ya existe notificación enviada para este voluntario y entidad
     */
    boolean existsByVolunteerIdAndEntityIdAndSentTrue(UUID volunteerId, UUID entityId);

    /**
     * Elimina las notificaciones que ya han sido enviadas y que fueron creadas antes de una fecha dada. Esto se puede usar para limpiar la tabla de notificaciones pendientes
     * y evitar que crezca indefinidamente con registros antiguos.
     */
    @Modifying
    @Query("DELETE FROM PendingNotification pn WHERE pn.sent = true AND pn.createdAt < :cutoff")
    int deleteBysentTrueAndCreatedAtBefore(@Param("cutoff") LocalDateTime cutoff);
}