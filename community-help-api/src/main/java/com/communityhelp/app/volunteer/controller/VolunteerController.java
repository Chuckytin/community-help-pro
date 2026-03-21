package com.communityhelp.app.volunteer.controller;

import com.communityhelp.app.security.AppUserDetails;
import com.communityhelp.app.volunteer.dto.VolunteerCreateRequestDto;
import com.communityhelp.app.volunteer.dto.VolunteerResponseDto;
import com.communityhelp.app.volunteer.dto.VolunteerUpdateRequestDto;
import com.communityhelp.app.volunteer.service.VolunteerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Volunteers", description = "Volunteer profile management. A user must register as a volunteer to receive proposals.")
@RestController
@RequestMapping("/api/v1/volunteers")
@RequiredArgsConstructor
public class VolunteerController {

    private final VolunteerService volunteerService;

    @Operation(summary = "Register as a volunteer")
    @ApiResponse(responseCode = "201", description = "Volunteer profile created")
    @PostMapping("/me")
    public ResponseEntity<VolunteerResponseDto> createVolunteer (
            @AuthenticationPrincipal AppUserDetails user,
            @Valid @RequestBody VolunteerCreateRequestDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(volunteerService.create(user.getId(), dto));
    }

    @Operation(summary = "Get my volunteer profile")
    @GetMapping("/me")
    public ResponseEntity<VolunteerResponseDto> getVolunteer (
            @AuthenticationPrincipal AppUserDetails user) {

        return ResponseEntity.ok(volunteerService.getMyProfile(user.getId()));
    }

    @Operation(summary = "Update my volunteer profile",
            description = "Update availability, action radius and skills")
    @PatchMapping("/me")
    public ResponseEntity<VolunteerResponseDto> updateVolunteer (
            @AuthenticationPrincipal AppUserDetails user,
            @Valid @RequestBody VolunteerUpdateRequestDto dto) {

        return ResponseEntity.ok(volunteerService.update(user.getId(), dto));
    }

    @Operation(summary = "Delete my volunteer profile")
    @ApiResponse(responseCode = "204", description = "Volunteer profile deleted")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteVolunteer (
            @AuthenticationPrincipal AppUserDetails user) {

        volunteerService.delete(user.getId());
        return ResponseEntity.noContent().build();
    }

}
