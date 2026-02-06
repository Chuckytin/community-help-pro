package com.communityhelp.app.donation.service;

import com.communityhelp.app.donation.dto.DonationCreateRequestDto;
import com.communityhelp.app.donation.dto.DonationResponseDto;
import com.communityhelp.app.donation.dto.DonationUpdateRequestDto;
import com.communityhelp.app.donation.model.DonationStatus;

import java.util.List;
import java.util.UUID;

public interface DonationService {

    // CRUD base
    DonationResponseDto createDonation(UUID donorId, DonationCreateRequestDto dto);

    DonationResponseDto getDonationById(UUID id);

    List<DonationResponseDto> getMyDonations(UUID donorId);

    List<DonationResponseDto> getDonationsByStatus(UUID donorId, DonationStatus status);

    List<DonationResponseDto> getDonationsAssignedToVolunteer(UUID volunteerId);

    DonationResponseDto updateDonation(UUID id, UUID donorId, DonationUpdateRequestDto dto);

    void deleteDonation(UUID id, UUID donorId);

    void deleteDonationAsAdmin(UUID id);

    // acciones de negocio
    DonationResponseDto reserveDonation(UUID id, UUID volunteerId);

    DonationResponseDto confirmDonation(UUID id, UUID donorId);

    DonationResponseDto pickupDonation(UUID id, UUID volunteerId);

    DonationResponseDto cancelDonation(UUID id, UUID donorId);

}
