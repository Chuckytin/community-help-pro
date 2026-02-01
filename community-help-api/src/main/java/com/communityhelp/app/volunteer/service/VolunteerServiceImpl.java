package com.communityhelp.app.volunteer.service;

import com.communityhelp.app.user.model.User;
import com.communityhelp.app.user.repository.UserRepository;
import com.communityhelp.app.volunteer.dto.VolunteerCreateRequestDto;
import com.communityhelp.app.volunteer.dto.VolunteerResponseDto;
import com.communityhelp.app.volunteer.dto.VolunteerUpdateRequestDto;
import com.communityhelp.app.volunteer.mapper.VolunteerMapper;
import com.communityhelp.app.volunteer.model.Volunteer;
import com.communityhelp.app.volunteer.repository.VolunteerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VolunteerServiceImpl implements VolunteerService {

    private final VolunteerRepository volunteerRepository;
    private final UserRepository userRepository;
    private final VolunteerMapper volunteerMapper;

    @Override
    @Transactional
    public VolunteerResponseDto create(UUID userId, VolunteerCreateRequestDto dto) {
        if (volunteerRepository.existsByUser_Id(userId)) {
            throw new IllegalStateException("User is already a volunteer");
        }

        User user = userRepository.findById(userId).orElseThrow();

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
    @Transactional
    public VolunteerResponseDto update(UUID userId, VolunteerUpdateRequestDto dto) {
        Volunteer volunteer = volunteerRepository.findByUser_Id(userId)
                .orElseThrow();

        if (dto.getAvailable() != null) volunteer.setAvailable(dto.getAvailable());
        if (dto.getRadiusKm() != null) volunteer.setRadiusKm(dto.getRadiusKm());
        if (dto.getSkills() != null) volunteer.setSkills(dto.getSkills());

        Volunteer savedVolunteer = volunteerRepository.save(volunteer);

        return volunteerMapper.toDto(savedVolunteer);
    }

    @Override
    @Transactional
    public void delete(UUID userId) {
        volunteerRepository.deleteByUser_Id(userId);
    }

    @Override
    public VolunteerResponseDto getMyProfile(UUID userId) {
        Volunteer volunteer = volunteerRepository.findByUser_Id(userId)
                .orElseThrow();

        return volunteerMapper.toDto(volunteer);
    }
}
