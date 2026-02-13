package com.communityhelp.app.review.controller;

import com.communityhelp.app.review.dto.ReviewCreateRequestDto;
import com.communityhelp.app.review.dto.ReviewResponseDto;
import com.communityhelp.app.review.dto.ReviewUpdateRequestDto;
import com.communityhelp.app.review.service.ReviewService;
import com.communityhelp.app.security.AppUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponseDto> createReview(
            @AuthenticationPrincipal AppUserDetails user,
            @Valid @RequestBody ReviewCreateRequestDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.create(user.getId(), dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserDetails user,
            @Valid @RequestBody ReviewUpdateRequestDto dto) {

        return ResponseEntity.ok(
                reviewService.update(id, user.getId(), dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserDetails user) {

        reviewService.delete(id, user.getId());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{targetId}")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsForUser(
            @PathVariable UUID targetId) {

        return ResponseEntity.ok(
                reviewService.getReviewsForUser(targetId)
        );
    }

}
