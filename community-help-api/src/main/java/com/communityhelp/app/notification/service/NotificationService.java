package com.communityhelp.app.notification.service;

import java.util.UUID;

public interface NotificationService {

    void enqueueProposalNotification(UUID volunteerId, String email,
                                     String name, String entityTitle,
                                     String entityType);

    void sendDigests();

}
