package com.communityhelp.app.chat.conversation.controller;

import com.communityhelp.app.chat.conversation.dto.ConversationResponseDto;
import com.communityhelp.app.chat.conversation.service.ConversationService;
import com.communityhelp.app.chat.message.dto.MessageCreateRequestDto;
import com.communityhelp.app.chat.message.dto.MessageResponseDto;
import com.communityhelp.app.security.AppUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

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

}