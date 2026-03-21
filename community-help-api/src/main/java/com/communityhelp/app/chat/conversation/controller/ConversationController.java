package com.communityhelp.app.chat.conversation.controller;

import com.communityhelp.app.chat.conversation.dto.ConversationResponseDto;
import com.communityhelp.app.chat.conversation.service.ConversationService;
import com.communityhelp.app.chat.message.dto.MessageCreateRequestDto;
import com.communityhelp.app.chat.message.dto.MessageResponseDto;
import com.communityhelp.app.security.AppUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Chat", description = "REST messaging. For real-time use the WebSocket endpoint at /ws with STOMP.")
@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @Operation(summary = "Get or create a conversation",
            description = "Creates the conversation if it does not exist for the given entity")
    @ApiResponse(responseCode = "201", description = "Conversation created or retrieved")
    @PostMapping
    public ResponseEntity<ConversationResponseDto> getOrCreateConversation(
            @RequestParam UUID relatedEntityId,
            @RequestParam String type,
            @AuthenticationPrincipal AppUserDetails userDetails,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                conversationService.getOrCreateConversation(
                        relatedEntityId,
                        type,
                        userDetails.getId(),
                        authentication
                ));
    }

    @Operation(summary = "Get my conversations")
    @GetMapping
    public ResponseEntity<Page<ConversationResponseDto>> getUserConversations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AppUserDetails userDetails,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                conversationService.getUserConversations(
                        userDetails.getId(),
                        page,
                        size,
                        authentication
                )
        );
    }

    @Operation(summary = "Send a message via REST",
            description = "HTTP alternative to WebSocket for sending messages")
    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<MessageResponseDto> sendMessage(
            @PathVariable UUID conversationId,
            @Valid @RequestBody MessageCreateRequestDto dto,
            @AuthenticationPrincipal AppUserDetails userDetails,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                conversationService.sendMessage(
                        conversationId,
                        userDetails.getId(),
                        dto,
                        authentication
                ));
    }

    @Operation(summary = "Get messages from a conversation")
    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<Page<MessageResponseDto>> getMessages(
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AppUserDetails userDetails,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                conversationService.getMessages(
                        conversationId,
                        userDetails.getId(),
                        page,
                        size,
                        authentication
                ));
    }

    @Operation(summary = "Delete a message")
    @ApiResponse(responseCode = "204", description = "Message deleted")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{conversationId}/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable UUID conversationId,
            @PathVariable UUID messageId,
            Authentication authentication,
            @AuthenticationPrincipal AppUserDetails userDetails
    ) {
        conversationService.deleteMessage(
                conversationId,
                messageId,
                userDetails.getId(),
                authentication
        );

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Mark conversation as read")
    @ApiResponse(responseCode = "204", description = "Conversation marked as read")
    @PatchMapping("/{conversationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID conversationId,
                                           @AuthenticationPrincipal AppUserDetails userDetails,
                                           Authentication authentication) {

        conversationService.markConversationAsRead(
                conversationId,
                userDetails.getId(),
                authentication
        );

        return ResponseEntity.noContent().build();
    }

}