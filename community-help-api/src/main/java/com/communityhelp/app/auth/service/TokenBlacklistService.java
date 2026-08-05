package com.communityhelp.app.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mantiene en memoria los tokens JWT invalidados por logout, hasta su expiración natural.
 * Permite un logout efectivo pese a que el JWT es
 * stateless por diseño.
 */
@Service
@Slf4j
public class TokenBlacklistService {

    private final Map<String, Instant> blacklist = new ConcurrentHashMap<>();

    public void blacklist(String token, Instant expiresAt) {
        blacklist.put(token, expiresAt);
    }

    public boolean isBlacklisted(String token) {
        return blacklist.containsKey(token);
    }

    /**
     * Purga tokens ya expirados de forma natural, evitando crecimiento indefinido de memoria.
     */
    @Scheduled(fixedRateString = "${jwt.expiration-ms}")
    public void cleanupExpired() {
        Instant now = Instant.now();
        int before = blacklist.size();
        blacklist.values().removeIf(expiresAt -> expiresAt.isBefore(now));
        log.debug("[token-blacklist] Cleanup: {} -> {} entries", before, blacklist.size());
    }
}