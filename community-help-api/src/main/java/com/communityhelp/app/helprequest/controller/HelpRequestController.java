package com.communityhelp.app.helprequest.controller;

import com.communityhelp.app.helprequest.dto.HelpRequestCreateRequestDto;
import com.communityhelp.app.helprequest.dto.HelpRequestResponseDto;
import com.communityhelp.app.helprequest.dto.HelpRequestUpdateRequestDto;
import com.communityhelp.app.helprequest.model.HelpRequestStatus;
import com.communityhelp.app.helprequest.service.HelpRequestService;
import com.communityhelp.app.security.AppUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/help-requests")
@RequiredArgsConstructor
public class HelpRequestController {

    private final HelpRequestService helpRequestService;

    @PostMapping
    public ResponseEntity<HelpRequestResponseDto> createHelpRequest (
            @AuthenticationPrincipal AppUserDetails currentUser,
            @Valid @RequestBody HelpRequestCreateRequestDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(helpRequestService.createHelpRequest(currentUser.getId(), dto));
    }

    @GetMapping("/me")
    public ResponseEntity<List<HelpRequestResponseDto>> getMyHelpRequests (
            @AuthenticationPrincipal AppUserDetails currentUser) {

        return ResponseEntity.ok(
                helpRequestService.getMyHelpRequests(currentUser.getId())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<HelpRequestResponseDto> getHelpRequestById (@PathVariable UUID id) {
        return ResponseEntity.ok(helpRequestService.getHelpRequestById(id));
    }

    @GetMapping("/assigned/me")
    public ResponseEntity<List<HelpRequestResponseDto>> getAssignedToVolunteer (
            @AuthenticationPrincipal AppUserDetails currentUser
    ) {
        return ResponseEntity.ok(helpRequestService.getAssignedToVolunteer(currentUser.getId()));
    }

    @GetMapping
    public ResponseEntity<List<HelpRequestResponseDto>> getByStatus(
            @RequestParam HelpRequestStatus status) {

        return ResponseEntity.ok(helpRequestService.getByStatus(status));
    }

    @GetMapping("/volunteer/{volunteerId}")
    public ResponseEntity<List<HelpRequestResponseDto>> getByVolunteerAndStatus(
            @PathVariable UUID volunteerId,
            @RequestParam HelpRequestStatus status) {

        return ResponseEntity.ok(helpRequestService.getByVolunteerAndStatus(volunteerId, status));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<HelpRequestResponseDto> updateHelpRequest (
            @AuthenticationPrincipal AppUserDetails currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody HelpRequestUpdateRequestDto dto) {

        HelpRequestResponseDto helpRequestResponseDto =
                helpRequestService.updateHelpRequest(id, currentUser.getId(), dto);

        return ResponseEntity.ok(helpRequestResponseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHelpRequest (
            @AuthenticationPrincipal AppUserDetails currentUser,
            @PathVariable UUID id) {

        helpRequestService.deleteHelpRequest(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/admin")
    public ResponseEntity<Void> deleteHelpRequestAsAdmin(@PathVariable UUID id) {
        helpRequestService.deleteHelpRequestAsAdmin(id);
        return ResponseEntity.noContent().build();
    }

    // ACCIONES DE NEGOCIO

    @PostMapping("/{id}/accept")
    public ResponseEntity<HelpRequestResponseDto> acceptHelpRequest (
            @AuthenticationPrincipal AppUserDetails currentUser,
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                helpRequestService.acceptHelpRequest(id, currentUser.getId())
        );
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<HelpRequestResponseDto> completeHelpRequest (
            @AuthenticationPrincipal AppUserDetails currentUser,
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                helpRequestService.completeHelpRequest(id, currentUser.getId())
        );
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<HelpRequestResponseDto> cancelHelpRequest (
            @AuthenticationPrincipal AppUserDetails currentUser,
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                helpRequestService.cancelHelpRequest(id, currentUser.getId())
        );
    }
}
