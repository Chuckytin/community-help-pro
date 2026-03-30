package com.communityhelp.app.donation.controller;

import com.communityhelp.app.donation.dto.DonationCreateRequestDto;
import com.communityhelp.app.donation.dto.DonationResponseDto;
import com.communityhelp.app.donation.dto.DonationUpdateRequestDto;
import com.communityhelp.app.donation.model.DonationStatus;
import com.communityhelp.app.donation.service.DonationService;
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

@Tag(name = "Donations", description = "Goods donations. Lifecycle: AVAILABLE → RESERVED → CONFIRMED → PICKED_UP → COMPLETED.")
@RestController
@RequestMapping(path = "/api/v1/donations")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;

    @Operation(summary = "Create a donation",
            description = "Automatically triggers the matching engine to find nearby volunteers")
    @ApiResponse(responseCode = "201", description = "Donation created and matching started")
    @PostMapping
    public ResponseEntity<DonationResponseDto> createDonation(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @Valid @RequestBody DonationCreateRequestDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(donationService.createDonation(currentUser.getId(), dto));
    }

    @Operation(summary = "Get donation by ID")
    @GetMapping("/{id}")
    public ResponseEntity<DonationResponseDto> getDonationById(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @PathVariable UUID id) {
        return ResponseEntity.ok(donationService.getDonationById(id, currentUser.getId()));
    }

    @Operation(summary = "Get my donations")
    @GetMapping("/me")
    public ResponseEntity<Page<DonationResponseDto>> getMyDonations(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        return ResponseEntity.ok(
                donationService.getMyDonations(currentUser.getId(), page, size)
        );
    }

    @Operation(summary = "Get my donations filtered by status")
    @GetMapping("/me/status/{status}")
    public ResponseEntity<Page<DonationResponseDto>> getMyDonationsByStatus(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @PathVariable DonationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        return ResponseEntity.ok(
                donationService.getDonationsByStatus(currentUser.getId(), status, page, size)
        );
    }

    @Operation(summary = "Get donations assigned to me as volunteer")
    @GetMapping("/assigned/me")
    public ResponseEntity<Page<DonationResponseDto>> getAssigned(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        return ResponseEntity.ok(
                donationService.getDonationsAssignedToVolunteer(currentUser.getId(), page, size)
        );
    }

    @Operation(summary = "Update a donation")
    @PatchMapping("/{id}")
    public ResponseEntity<DonationResponseDto> updateDonation(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody DonationUpdateRequestDto dto) {

        return ResponseEntity.ok(
                donationService.updateDonation(id, currentUser.getId(), dto)
        );
    }

    @Operation(summary = "Delete a donation")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDonation(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @PathVariable UUID id) {

        donationService.deleteDonation(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete a donation (admin)", description = "Admin only")
    @ApiResponse(responseCode = "204", description = "Donation deleted")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/admin")
    public ResponseEntity<Void> deleteDonationAsAdmin(@PathVariable UUID id) {

        donationService.deleteDonationAsAdmin(id);
        return ResponseEntity.noContent().build();
    }

    // ACCIONES DE NEGOCIO

    @Operation(summary = "Reserve a donation",
            description = "The volunteer reserves the donation for pickup")
    @PostMapping("/{id}/reserve")
    public ResponseEntity<DonationResponseDto> reserveDonation(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                donationService.reserveDonation(id, currentUser.getId())
        );
    }

    @Operation(summary = "Confirm a donation",
            description = "The donor confirms the reservation is valid")
    @PostMapping("/{id}/confirm")
    public ResponseEntity<DonationResponseDto> confirmDonation(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                donationService.confirmDonation(id, currentUser.getId())
        );
    }

    @Operation(summary = "Mark as picked up",
            description = "The volunteer confirms they have collected the item")
    @PostMapping("/{id}/pickup")
    public ResponseEntity<DonationResponseDto> pickupDonation(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                donationService.pickupDonation(id, currentUser.getId())
        );
    }

    @Operation(summary = "Complete a donation",
            description = "The item has reached its destination")
    @PostMapping("/{id}/complete")
    public ResponseEntity<DonationResponseDto> completeDonation(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                donationService.completeDonation(id, currentUser.getId())
        );
    }

    @Operation(summary = "Cancel a donation")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<DonationResponseDto> cancelDonation(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                donationService.cancelDonation(id, currentUser.getId())
        );
    }

}
