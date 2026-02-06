package com.communityhelp.app.volunteer.service;

import com.communityhelp.app.donation.repository.DonationRepository;
import com.communityhelp.app.helprequest.repository.HelpRequestRepository;
import com.communityhelp.app.user.model.User;
import com.communityhelp.app.user.repository.UserRepository;
import com.communityhelp.app.volunteer.dto.VolunteerCreateRequestDto;
import com.communityhelp.app.volunteer.dto.VolunteerResponseDto;
import com.communityhelp.app.volunteer.dto.VolunteerUpdateRequestDto;
import com.communityhelp.app.volunteer.mapper.VolunteerMapper;
import com.communityhelp.app.volunteer.model.Volunteer;
import com.communityhelp.app.volunteer.repository.VolunteerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class VolunteerServiceImpl implements VolunteerService {

    private final VolunteerRepository volunteerRepository;
    private final HelpRequestRepository helpRequestRepository;
    private final DonationRepository donationRepository;
    private final UserRepository userRepository;
    private final VolunteerMapper volunteerMapper;

    @Override
    public VolunteerResponseDto create(UUID userId, VolunteerCreateRequestDto dto) {
        if (volunteerRepository.existsByUser_Id(userId)) {
            throw new IllegalStateException("User is already a volunteer");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        Volunteer volunteer = Volunteer.builder()
                .user(user)
                .available(dto.getAvailable() != null ? dto.getAvailable() : true)
                .radiusKm(dto.getRadiusKm())
                .skills(dto.getSkills())
                .build();

        Volunteer savedVolunteer = volunteerRepository.save(volunteer);

        return volunteerMapper.toDto(savedVolunteer);
    }

    @Override
    public VolunteerResponseDto update(UUID userId, VolunteerUpdateRequestDto dto) {
        Volunteer volunteer = volunteerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new EntityNotFoundException("Volunteer not found for user with ID: " + userId));

        if (dto.getAvailable() != null) volunteer.setAvailable(dto.getAvailable());
        if (dto.getRadiusKm() != null) volunteer.setRadiusKm(dto.getRadiusKm());
        if (dto.getSkills() != null) volunteer.setSkills(dto.getSkills());

        Volunteer savedVolunteer = volunteerRepository.save(volunteer);

        return volunteerMapper.toDto(savedVolunteer);
    }

    @Override
    public void delete(UUID userId) {
        String reason = "Cancelled because user deleted account";

        helpRequestRepository.releaseHelpRequestsAsVolunteer(userId, reason);
        donationRepository.releaseDonationsAsVolunteer(userId, reason);

        volunteerRepository.deleteById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public VolunteerResponseDto getMyProfile(UUID userId) {
        Volunteer volunteer = volunteerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new EntityNotFoundException("Volunteer not found for user with ID: " + userId));

        return volunteerMapper.toDto(volunteer);
    }
}
