package com.communityhelp.app.notification.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pending_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID volunteerId;

    @Column(nullable = false)
    private String volunteerEmail;

    @Column(nullable = false)
    private String volunteerName;

    @Column(nullable = false)
    private String entityTitle;

    @Column(nullable = false)
    private String entityType;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean sent = false;

}