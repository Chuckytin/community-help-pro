package com.communityhelp.app.proposal.controller;

import com.communityhelp.app.proposal.dto.ProposalResponseDto;
import com.communityhelp.app.proposal.model.ProposalStatus;
import com.communityhelp.app.proposal.service.ProposalService;
import com.communityhelp.app.security.AppUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/proposals")
@RequiredArgsConstructor
public class ProposalController {

    private final ProposalService proposalService;

    @GetMapping("/volunteer")
    public ResponseEntity<Page<ProposalResponseDto>> getByVolunteer(
            @AuthenticationPrincipal AppUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                proposalService.getProposalsByVolunteer(userDetails.getId(), page, size)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<ProposalResponseDto>> getByStatus(
            @PathVariable ProposalStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                proposalService.getProposalsByStatus(status, page, size)
        );
    }

    @GetMapping("/entity/{entityId}")
    public ResponseEntity<ProposalResponseDto> getProposal(
            @PathVariable UUID entityId,
            @AuthenticationPrincipal AppUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                proposalService.getProposal(userDetails.getId(), entityId)
        );
    }

    @PostMapping("/{proposalId}/accept")
    public ResponseEntity<ProposalResponseDto> acceptProposal(
            @PathVariable UUID proposalId,
            @AuthenticationPrincipal AppUserDetails user
    ) {

        return ResponseEntity.ok(
                proposalService.acceptProposal(user.getId(), proposalId)
        );
    }

    @PostMapping("/{proposalId}/reject")
    public ResponseEntity<ProposalResponseDto> rejectProposal(
            @PathVariable UUID proposalId,
            @AuthenticationPrincipal AppUserDetails user
    ) {

        return ResponseEntity.ok(
                proposalService.rejectProposal(user.getId(), proposalId)
        );
    }
}