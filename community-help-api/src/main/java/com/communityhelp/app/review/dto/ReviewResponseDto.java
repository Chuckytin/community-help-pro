package com.communityhelp.app.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO que se enviará al frontend con información de la Review
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {

    private UUID id;

    private UUID authorId;
    private String authorName;

    private UUID targetId;
    private String targetName;

    private UUID donationId;
    private UUID helpRequestId;

    private Integer rating;
    private String comment;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

