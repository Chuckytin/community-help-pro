package com.communityhelp.app.helprequest.controller;

import com.communityhelp.app.helprequest.dto.HelpRequestCreateRequestDto;
import com.communityhelp.app.helprequest.dto.HelpRequestResponseDto;
import com.communityhelp.app.helprequest.dto.HelpRequestUpdateRequestDto;
import com.communityhelp.app.helprequest.model.HelpRequestStatus;
import com.communityhelp.app.helprequest.service.HelpRequestService;
import com.communityhelp.app.security.AppUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Help Requests", description = "Requests for specific help. Creating one triggers the automatic matching engine for nearby volunteers.")
@RestController
@RequestMapping("/api/v1/help-requests")
@RequiredArgsConstructor
public class HelpRequestController {

    private final HelpRequestService helpRequestService;

    @Operation(summary = "Create a help request",
            description = "Automatically triggers the matching engine to find nearby volunteers")
    @ApiResponse(responseCode = "201", description = "Request created and matching started")
    @PostMapping
    public ResponseEntity<HelpRequestResponseDto> createHelpRequest(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @Valid @RequestBody HelpRequestCreateRequestDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(helpRequestService.createHelpRequest(currentUser.getId(), dto));
    }

    @Operation(summary = "Get my help requests as requester")
    @GetMapping("/me")
    public ResponseEntity<Page<HelpRequestResponseDto>> getMyHelpRequests(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) HelpRequestStatus status
    ) {

        return ResponseEntity.ok(
                helpRequestService.getMyHelpRequests(currentUser.getId(), page, size, status)
        );
    }

    @Operation(summary = "Get my specific help request")
    @GetMapping("/me/{id}")
    public ResponseEntity<HelpRequestResponseDto> getMyHelpRequestById(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                helpRequestService.getMyHelpRequestById(currentUser.getId(), id)
        );
    }

    @Operation(summary = "Get all open help requests",
            description = "Public marketplace - shows available requests")
    @GetMapping
    public ResponseEntity<Page<HelpRequestResponseDto>> getOpenHelpRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                helpRequestService.getOpenHelpRequests(page, size)
        );
    }

    @Operation(summary = "Get requests assigned to me as volunteer")
    @GetMapping("/assigned/me")
    public ResponseEntity<Page<HelpRequestResponseDto>> getAssignedToVolunteer(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) HelpRequestStatus status
    ) {
        return ResponseEntity.ok(
                helpRequestService.getAssignedToVolunteer(currentUser.getId(), page, size, status)
        );
    }

    @Operation(summary = "Get help request by ID", description = "Admin only")
    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HelpRequestResponseDto> getHelpRequestById(@PathVariable UUID id) {
        return ResponseEntity.ok(helpRequestService.getHelpRequestById(id));
    }

    @Operation(summary = "Get requests by status", description = "Admin only")
    @GetMapping("/admin/by-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<HelpRequestResponseDto>> getByStatus(
            @RequestParam HelpRequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        return ResponseEntity.ok(helpRequestService.getByStatus(status, page, size));
    }

    @Operation(summary = "Get volunteer's requests", description = "Admin only")
    @GetMapping("/admin/volunteer/{volunteerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<HelpRequestResponseDto>> getByVolunteer(
            @PathVariable UUID volunteerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        return ResponseEntity.ok(helpRequestService.getByVolunteer(volunteerId, page, size));
    }

    @Operation(summary = "Update a help request",
            description = "Only OPEN requests can be updated. Regenerates proposals if location or type changes")
    @PatchMapping("/{id}")
    public ResponseEntity<HelpRequestResponseDto> updateHelpRequest(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody HelpRequestUpdateRequestDto dto) {

        HelpRequestResponseDto helpRequestResponseDto =
                helpRequestService.updateHelpRequest(id, currentUser.getId(), dto);

        return ResponseEntity.ok(helpRequestResponseDto);
    }

    @Operation(summary = "Delete a help request")
    @ApiResponse(responseCode = "204", description = "Request deleted")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHelpRequest(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @PathVariable UUID id) {

        helpRequestService.deleteHelpRequest(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete a help request (admin)", description = "Admin only")
    @ApiResponse(responseCode = "204", description = "Request deleted")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/admin")
    public ResponseEntity<Void> deleteHelpRequestAsAdmin(@PathVariable UUID id) {
        helpRequestService.deleteHelpRequestAsAdmin(id);
        return ResponseEntity.noContent().build();
    }

    // ACCIONES DE NEGOCIO

    @Operation(summary = "Accept a help request as volunteer",
            description = "Assigns the volunteer to the request and cancels all other pending proposals")
    @PostMapping("/{id}/accept")
    public ResponseEntity<HelpRequestResponseDto> acceptHelpRequest(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                helpRequestService.acceptHelpRequest(id, currentUser.getId())
        );
    }

    @Operation(summary = "Mark as completed",
            description = "Only the assigned volunteer can complete the request")
    @PostMapping("/{id}/complete")
    public ResponseEntity<HelpRequestResponseDto> completeHelpRequest(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                helpRequestService.completeHelpRequest(id, currentUser.getId())
        );
    }

    @Operation(summary = "Cancel a help request")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<HelpRequestResponseDto> cancelHelpRequest(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                helpRequestService.cancelHelpRequest(id, currentUser.getId())
        );
    }
}
