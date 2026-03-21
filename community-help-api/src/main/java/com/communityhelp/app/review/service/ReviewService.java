package com.communityhelp.app.review.service;

import com.communityhelp.app.review.dto.ReviewCreateRequestDto;
import com.communityhelp.app.review.dto.ReviewResponseDto;
import com.communityhelp.app.review.dto.ReviewUpdateRequestDto;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface ReviewService {

    ReviewResponseDto create(UUID authorId, ReviewCreateRequestDto dto);

    ReviewResponseDto update(UUID reviewId, UUID authorId, ReviewUpdateRequestDto dto);

    void delete(UUID reviewId, UUID authorId);

    Page<ReviewResponseDto> getReviewsForUser(UUID targetId, int page, int size);

}
