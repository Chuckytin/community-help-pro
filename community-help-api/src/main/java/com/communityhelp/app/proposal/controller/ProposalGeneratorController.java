package com.communityhelp.app.proposal.controller;

import com.communityhelp.app.donation.model.Donation;
import com.communityhelp.app.helprequest.model.HelpRequest;
import com.communityhelp.app.proposal.service.ProposalGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Proposals", description = "Manual matching trigger (admin only)")
@RestController
@RequestMapping("/api/v1/proposals/generate")
@RequiredArgsConstructor
public class ProposalGeneratorController {

    private final ProposalGeneratorService generatorService;

    /**
     * Genera proposals automáticas para una HelpRequest específica.
     * Solo admins pueden dispararlo manualmente.
     */
    @Operation(summary = "Generate proposals for a HelpRequest",
            description = "Manually triggers the matching engine. Useful in development or to force a retry.")
    @ApiResponse(responseCode = "200", description = "Matching triggered")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/help-request/{helpRequestId}")
    public ResponseEntity<Void> generateForHelpRequest(
            @PathVariable UUID helpRequestId
    ) {
        HelpRequest helpRequest = generatorService.getHelpRequestById(helpRequestId);
        generatorService.generateForHelpRequest(helpRequest);
        return ResponseEntity.ok().build();
    }

    /**
     * Genera proposals automáticas para una Donation específica.
     * Solo admins pueden dispararlo manualmente.
     */
    @Operation(summary = "Generate proposals for a Donation",
            description = "Manually triggers the matching engine. Useful in development or to force a retry.")
    @ApiResponse(responseCode = "200", description = "Matching triggered")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/donation/{donationId}")
    public ResponseEntity<Void> generateForDonation(
            @PathVariable UUID donationId
    ) {
        Donation donation = generatorService.getDonationById(donationId);
        generatorService.generateForDonation(donation);
        return ResponseEntity.ok().build();
    }
}