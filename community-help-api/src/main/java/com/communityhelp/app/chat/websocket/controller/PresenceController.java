package com.communityhelp.app.chat.websocket.controller;

import com.communityhelp.app.chat.websocket.service.PresenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

/**
 * Endpoint REST para consultar usuarios actualmente online.
 */
@Tag(name = "Presence", description = "Real-time online users via WebSocket")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/presence")
public class PresenceController {

    private final PresenceService presenceService;

    @Operation(summary = "Get online users",
            description = "Returns the IDs of users with an active WebSocket connection")
    @GetMapping
    public Set<UUID> getOnlineUsers() {
        return presenceService.getOnlineUsers();
    }

}
