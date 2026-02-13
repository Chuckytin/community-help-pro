package com.communityhelp.app.donation.controller;

import com.communityhelp.app.donation.dto.DonationCreateRequestDto;
import com.communityhelp.app.donation.dto.DonationResponseDto;
import com.communityhelp.app.donation.dto.DonationUpdateRequestDto;
import com.communityhelp.app.donation.model.DonationStatus;
import com.communityhelp.app.donation.service.DonationService;
import com.communityhelp.app.helprequest.dto.HelpRequestResponseDto;
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
@RequestMapping(path = "/api/v1/donations")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;

    @PostMapping
    public ResponseEntity<DonationResponseDto> createDonation (
            @AuthenticationPrincipal AppUserDetails currentUser,
            @Valid @RequestBody DonationCreateRequestDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(donationService.createDonation(currentUser.getId(), dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DonationResponseDto> getDonationById (@PathVariable UUID id) {
        return ResponseEntity.ok(donationService.getDonationById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<List<DonationResponseDto>> getMyDonations (
            @AuthenticationPrincipal AppUserDetails currentUser) {

        return ResponseEntity.ok(
                donationService.getMyDonations(currentUser.getId())
        );
    }

    @GetMapping("/me/status/{status}")
    public ResponseEntity<List<DonationResponseDto>> getMyDonationsByStatus (
            @AuthenticationPrincipal AppUserDetails currentUser,
            @PathVariable DonationStatus status) {

        return ResponseEntity.ok(
                donationService.getDonationsByStatus(currentUser.getId(), status)
        );
    }

    @GetMapping("/assigned/me")
    public ResponseEntity<List<DonationResponseDto>> getAssigned(
            @AuthenticationPrincipal AppUserDetails currentUser) {

        return ResponseEntity.ok(
                donationService.getDonationsAssignedToVolunteer(currentUser.getId())
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DonationResponseDto> updateDonation (
            @AuthenticationPrincipal AppUserDetails currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody DonationUpdateRequestDto dto) {

        return ResponseEntity.ok(
                donationService.updateDonation(id, currentUser.getId(), dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDonation(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @PathVariable UUID id) {

        donationService.deleteDonation(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/admin")
    public ResponseEntity<Void> deleteDonationAsAdmin(@PathVariable UUID id) {

        donationService.deleteDonationAsAdmin(id);
        return ResponseEntity.noContent().build();
    }

    // ACCIONES DE NEGOCIO

    @PostMapping("/{id}/reserve")
    public ResponseEntity<DonationResponseDto> reserveDonation(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                donationService.reserveDonation(id, currentUser.getId())
        );
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<DonationResponseDto> confirmDonation(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                donationService.confirmDonation(id, currentUser.getId())
        );
    }

    @PostMapping("/{id}/pickup")
    public ResponseEntity<DonationResponseDto> pickupDonation(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                donationService.pickupDonation(id, currentUser.getId())
        );
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<DonationResponseDto> completeDonation (
            @AuthenticationPrincipal AppUserDetails currentUser,
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                donationService.completeDonation(id, currentUser.getId())
        );
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<DonationResponseDto> cancelDonation(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                donationService.cancelDonation(id, currentUser.getId())
        );
    }

}
