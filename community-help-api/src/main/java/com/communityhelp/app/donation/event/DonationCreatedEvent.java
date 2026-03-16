package com.communityhelp.app.donation.event;

import java.util.UUID;

/**
 * Evento publicado cuando una Donation es creada.
 * Permite iniciar procesos automáticos como:
 * - Generación de proposals
 * - Notificaciones
 * - Analytics
 */
public record DonationCreatedEvent(UUID donationId) {
}