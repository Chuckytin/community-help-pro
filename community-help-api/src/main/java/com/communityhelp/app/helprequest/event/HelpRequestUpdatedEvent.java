package com.communityhelp.app.helprequest.event;

import java.util.UUID;

/**
 * Evento publicado cuando una HelpRequest es actualizada.
 * Permite reaccionar de forma desacoplada a cambios que puedan
 * afectar el matching de voluntarios:
 * - cambio de ubicación
 * - cambio de tipo
 * - cambio de deadline
 * - cambio de estado
 * Consumido por ProposalGenerationListener para regenerar proposals.
 */
public record HelpRequestUpdatedEvent(UUID helpRequestId) {
}