package com.communityhelp.app.volunteer.event;

import java.util.UUID;

/**
 * Evento publicado cuando un voluntario cambia información relevante para el matching:
 * - ubicación
 * - skills
 * - disponibilidad
 * Permite recalcular automáticamente los scores de proposals.
 */
public record VolunteerUpdatedEvent(
        UUID volunteerId,
        boolean availableChanged,
        boolean isAvailable,
        boolean radiusChanged,
        boolean skillsChanged
) {}