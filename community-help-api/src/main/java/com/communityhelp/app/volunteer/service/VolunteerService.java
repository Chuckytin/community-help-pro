package com.communityhelp.app.volunteer.service;

import com.communityhelp.app.volunteer.dto.VolunteerCreateRequestDto;
import com.communityhelp.app.volunteer.dto.VolunteerResponseDto;
import com.communityhelp.app.volunteer.dto.VolunteerUpdateRequestDto;

import java.util.UUID;

public interface VolunteerService {

    VolunteerResponseDto create(UUID userId, VolunteerCreateRequestDto dto);

    VolunteerResponseDto update(UUID userId, VolunteerUpdateRequestDto dto);

    void delete(UUID userId);

    VolunteerResponseDto getMyProfile(UUID userId);

}
