package com.communityhelp.app.review.service;

import com.communityhelp.app.donation.model.Donation;
import com.communityhelp.app.donation.model.DonationStatus;
import com.communityhelp.app.donation.repository.DonationRepository;
import com.communityhelp.app.helprequest.model.HelpRequest;
import com.communityhelp.app.helprequest.model.HelpRequestStatus;
import com.communityhelp.app.helprequest.repository.HelpRequestRepository;
import com.communityhelp.app.review.dto.ReviewCreateRequestDto;
import com.communityhelp.app.review.dto.ReviewResponseDto;
import com.communityhelp.app.review.dto.ReviewUpdateRequestDto;
import com.communityhelp.app.review.mapper.ReviewMapper;
import com.communityhelp.app.review.model.Review;
import com.communityhelp.app.review.repository.ReviewRepository;
import com.communityhelp.app.user.model.User;
import com.communityhelp.app.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final DonationRepository donationRepository;
    private final HelpRequestRepository helpRequestRepository;
    private final ReviewMapper reviewMapper;

    /**
     * Crea una nueva Review para una Donation o HelpRequest.
     * 1. Valida que se referencie exactamente UNA entidad (donation XOR helpRequest).
     * 2. Obtiene author y target.
     * 3. Verifica que no exista ya una review del mismo author para esa entidad.
     * 4. Valida reglas de negocio:
     *    - La entidad debe estar en estado COMPLETED.
     *    - El author debe haber participado realmente en ella.
     * 5. Actualiza el rating del target.
     * 6. Persiste la review y devuelve el DTO.
     */
    @Override
    public ReviewResponseDto create(UUID authorId, ReviewCreateRequestDto dto) {

        if (dto.getDonationId() == null && dto.getHelpRequestId() == null) {
            throw new IllegalArgumentException("Review must reference a donation or helpRequest");
        }

        if (dto.getDonationId() != null && dto.getHelpRequestId() != null) {
            throw new IllegalArgumentException("Review cannot reference both donation and helpRequest");
        }

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Author not found"));

        User target = userRepository.findById(dto.getTargetId())
                .orElseThrow(() -> new EntityNotFoundException("Target not found"));

        Review review = Review.builder()
                .author(author)
                .target(target)
                .rating(dto.getRating())
                .comment(dto.getComment())
                .build();

        // DONATION
        if (dto.getDonationId() != null) {

            if (reviewRepository.existsByAuthor_IdAndDonation_Id(authorId, dto.getDonationId())) {
                throw new IllegalStateException("You already reviewed this donation");
            }

            Donation donation = donationRepository.findById(dto.getDonationId())
                    .orElseThrow(() -> new EntityNotFoundException("Donation not found"));

            if (donation.getStatus() != DonationStatus.COMPLETED) {
                throw new IllegalStateException("Cannot review a donation that is not completed");
            }

            boolean participated =
                    donation.getDonorId().equals(authorId) ||
                            (donation.getVolunteerId() != null && donation.getVolunteerId().equals(authorId));

            if (!participated) {
                throw new IllegalStateException("You did not participate in this donation");
            }

            review.setDonation(donation);

        }

        // HELP REQUEST
        if (dto.getHelpRequestId() != null) {

            if (reviewRepository.existsByAuthor_IdAndHelpRequest_Id(authorId, dto.getHelpRequestId())) {
                throw new IllegalStateException("You already reviewed this help request");
            }

            HelpRequest helpRequest = helpRequestRepository.findById(dto.getHelpRequestId())
                    .orElseThrow(() -> new EntityNotFoundException("HelpRequest not found"));

            if (helpRequest.getStatus() != HelpRequestStatus.COMPLETED) {
                throw new IllegalStateException("Cannot review a help request that is not completed");
            }

            boolean participated =
                    helpRequest.getRequesterId().equals(authorId) ||
                            (helpRequest.getVolunteerId() != null && helpRequest.getVolunteerId().equals(authorId));

            if (!participated) {
                throw new IllegalStateException("You did not participate in this help request");
            }

            review.setHelpRequest(helpRequest);
        }

        Review saved = reviewRepository.save(review);

        updateUserRating(saved.getTargetId());

        return reviewMapper.toDto(saved);
    }

    @Override
    public ReviewResponseDto update(UUID reviewId, UUID authorId, ReviewUpdateRequestDto dto) {

        Review review = getOwnedReview(reviewId, authorId);

        review.setRating(dto.getRating());
        review.setComment(dto.getComment());

        updateUserRating(review.getTargetId());

        return reviewMapper.toDto(review);
    }

    @Override
    public void delete(UUID reviewId, UUID authorId) {
        Review review = getOwnedReview(reviewId, authorId);

        UUID targetId = review.getTargetId();

        reviewRepository.delete(review);

        updateUserRating(targetId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsForUser(UUID targetId) {

        return reviewRepository.findByTarget_Id(targetId)
                .stream()
                .map(reviewMapper::toDto)
                .toList();
    }

    /**
     * Helper para obtener una Review y valida que pertenezca al author indicado.
     */
    private Review getOwnedReview(UUID reviewId, UUID authorId) {

        Review review = reviewRepository.findWithAuthorAndTargetById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));

        if (!review.getAuthorId().equals(authorId)) {
            throw new IllegalStateException("Not allowed");
        }

        return review;
    }

    /**
     * Recalcula el promedio de las reviews del target.
     */
    private void updateUserRating(UUID userId) {
        Double avg = reviewRepository.calculateAverageRatingByTargetId(userId);

        float rating = avg != null ? avg.floatValue() : 0f;

        userRepository.updateRating(userId, rating);
    }


}

