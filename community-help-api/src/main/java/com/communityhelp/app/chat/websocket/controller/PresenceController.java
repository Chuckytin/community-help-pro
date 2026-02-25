package com.communityhelp.app.chat.websocket.controller;

import com.communityhelp.app.chat.websocket.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

/**
 * Endpoint REST para consultar usuarios actualmente online.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/presence")
public class PresenceController {

    private final PresenceService presenceService;

    @GetMapping
    public Set<UUID> getOnlineUsers() {
        return presenceService.getOnlineUsers();
    }

}
