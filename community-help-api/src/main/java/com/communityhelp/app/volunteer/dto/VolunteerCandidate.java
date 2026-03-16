package com.communityhelp.app.volunteer.dto;

import com.communityhelp.app.volunteer.model.Volunteer;

public record VolunteerCandidate(
        Volunteer volunteer,
        double distanceMeters
) {}
