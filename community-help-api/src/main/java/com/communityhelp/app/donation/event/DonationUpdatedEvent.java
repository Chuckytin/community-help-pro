package com.communityhelp.app.donation.event;

import java.util.UUID;

/**
 * Evento publicado cuando una Donation es modificada.
 * Permite recalcular automáticamente el matching entre
 * voluntarios y donaciones disponibles.
 * - cambio de cantidad
 * - cambio de ubicación
 * - cambio de fecha de expiración
 * - cambio de estado
 * Consumido por ProposalGenerationListener.
 */
public record DonationUpdatedEvent(UUID donationId) {
}