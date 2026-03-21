package com.communityhelp.app.proposal.matching;

import java.util.Map;
import java.util.UUID;

public record MatchingContext(
        double distanceMeters,
        Map<UUID, Long> pendingCounts,
        int retryCount
) {}
