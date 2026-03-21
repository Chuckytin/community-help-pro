package com.communityhelp.app.review.controller;

import com.communityhelp.app.review.dto.ReviewCreateRequestDto;
import com.communityhelp.app.review.dto.ReviewResponseDto;
import com.communityhelp.app.review.dto.ReviewUpdateRequestDto;
import com.communityhelp.app.review.service.ReviewService;
import com.communityhelp.app.security.AppUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Reviews", description = "Reviews between participants. Only users who participated in a completed Donation or HelpRequest can leave a review.")
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Create a review",
            description = "Must reference a completed Donation or HelpRequest in which the author participated")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Review created and user rating updated"),
            @ApiResponse(responseCode = "409", description = "You already reviewed this entity or it is not completed yet")
    })
    @PostMapping
    public ResponseEntity<ReviewResponseDto> createReview(
            @AuthenticationPrincipal AppUserDetails user,
            @Valid @RequestBody ReviewCreateRequestDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.create(user.getId(), dto));
    }

    @Operation(summary = "Update a review", description = "Only the author can modify it")
    @PatchMapping("/{id}")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserDetails user,
            @Valid @RequestBody ReviewUpdateRequestDto dto) {

        return ResponseEntity.ok(
                reviewService.update(id, user.getId(), dto)
        );
    }

    @Operation(summary = "Delete a review")
    @ApiResponse(responseCode = "204", description = "Review deleted and user rating recalculated")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserDetails user) {

        reviewService.delete(id, user.getId());

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get reviews for a user",
            description = "Returns the review history received by a user")
    @GetMapping("/users/{targetId}")
    public ResponseEntity<Page<ReviewResponseDto>> getReviewsForUser(
            @PathVariable UUID targetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        return ResponseEntity.ok(
                reviewService.getReviewsForUser(targetId, page, size)
        );
    }

}
