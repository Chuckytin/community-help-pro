package com.communityhelp.app.volunteer.controller;

import com.communityhelp.app.security.AppUserDetails;
import com.communityhelp.app.volunteer.dto.VolunteerCreateRequestDto;
import com.communityhelp.app.volunteer.dto.VolunteerResponseDto;
import com.communityhelp.app.volunteer.dto.VolunteerUpdateRequestDto;
import com.communityhelp.app.volunteer.service.VolunteerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/volunteers")
@RequiredArgsConstructor
public class VolunteerController {

    private final VolunteerService volunteerService;

    @PostMapping("/me")
    public ResponseEntity<VolunteerResponseDto> createVolunteer (
            @AuthenticationPrincipal AppUserDetails user,
            @RequestBody VolunteerCreateRequestDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(volunteerService.create(user.getId(), dto));
    }

    @GetMapping("/me")
    public ResponseEntity<VolunteerResponseDto> getVolunteer (
            @AuthenticationPrincipal AppUserDetails user) {

        return ResponseEntity.ok(volunteerService.getMyProfile(user.getId()));
    }

    @PatchMapping("/me")
    public ResponseEntity<VolunteerResponseDto> updateVolunteer (
            @AuthenticationPrincipal AppUserDetails user,
            @RequestBody VolunteerUpdateRequestDto dto) {

        return ResponseEntity.ok(volunteerService.update(user.getId(), dto));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteVolunteer (
            @AuthenticationPrincipal AppUserDetails user) {

        volunteerService.delete(user.getId());
        return ResponseEntity.noContent().build();
    }

}
