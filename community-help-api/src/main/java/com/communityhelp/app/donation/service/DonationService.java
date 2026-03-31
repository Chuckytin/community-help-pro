package com.communityhelp.app.donation.service;

import com.communityhelp.app.donation.dto.DonationCreateRequestDto;
import com.communityhelp.app.donation.dto.DonationResponseDto;
import com.communityhelp.app.donation.dto.DonationUpdateRequestDto;
import com.communityhelp.app.donation.model.DonationStatus;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface DonationService {

    // CRUD base
    DonationResponseDto createDonation(UUID donorId, DonationCreateRequestDto dto);

    DonationResponseDto getDonationById(UUID id, UUID currentUserId);

    Page<DonationResponseDto> getMyDonations(UUID donorId, int page, int size);

    Page<DonationResponseDto> getDonationsByStatus(UUID donorId, DonationStatus status, int page, int size);

    Page<DonationResponseDto> getDonationsAssignedToVolunteer(UUID volunteerId, int page, int size);

    Page<DonationResponseDto> findNearby(
            UUID currentUserId,
            double lat,
            double lon,
            double radiusMeters,
            String donationType,
            int page,
            int size
    );

    DonationResponseDto updateDonation(UUID id, UUID donorId, DonationUpdateRequestDto dto);

    void deleteDonation(UUID id, UUID donorId);

    void deleteDonationAsAdmin(UUID id);

    // acciones de negocio
    DonationResponseDto reserveDonation(UUID id, UUID volunteerId);

    DonationResponseDto confirmDonation(UUID id, UUID donorId);

    DonationResponseDto pickupDonation(UUID id, UUID volunteerId);

    DonationResponseDto completeDonation(UUID id, UUID volunteerId);

    DonationResponseDto cancelDonation(UUID id, UUID donorId);

}
