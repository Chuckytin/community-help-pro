package com.communityhelp.app.chat.websocket.service;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio encargado de gestionar la presencia en memoria.
 * Mantiene un Set concurrente con los usuarios conectados.
 */
@Getter
@Service
public class PresenceService {

    private final Set<UUID> onlineUsers = ConcurrentHashMap.newKeySet();

    public void userConnected(UUID userId) {
        onlineUsers.add(userId);
    }

    public void userDisconnected(UUID userId) {
        onlineUsers.remove(userId);
    }

    public boolean isOnline(UUID userId) {
        return onlineUsers.contains(userId);
    }

}
