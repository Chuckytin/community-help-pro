package com.communityhelp.app.helprequest.event;

import java.util.UUID;

/**
 * Evento de dominio publicado cuando se crea una nueva HelpRequest.
 * - Desacopla la creación de HelpRequests del sistema de generación automática de proposals.
 * - Permite añadir nuevos comportamientos reaccionando al evento sin modificar el servicio original.
 */
public record HelpRequestCreatedEvent(UUID helpRequestId) {
}