package com.communityhelp.app.chat.conversation.service;

import com.communityhelp.app.chat.conversation.dto.ConversationResponseDto;
import com.communityhelp.app.chat.conversation.mapper.ConversationMapper;
import com.communityhelp.app.chat.conversation.model.Conversation;
import com.communityhelp.app.chat.conversation.model.ConversationParticipant;
import com.communityhelp.app.chat.conversation.model.ConversationType;
import com.communityhelp.app.chat.conversation.repository.ConversationParticipantRepository;
import com.communityhelp.app.chat.conversation.repository.ConversationRepository;
import com.communityhelp.app.chat.message.dto.MessageCreateRequestDto;
import com.communityhelp.app.chat.message.dto.MessageResponseDto;
import com.communityhelp.app.chat.message.mapper.MessageMapper;
import com.communityhelp.app.chat.message.model.Message;
import com.communityhelp.app.chat.message.repository.MessageRepository;
import com.communityhelp.app.donation.model.Donation;
import com.communityhelp.app.donation.repository.DonationRepository;
import com.communityhelp.app.helprequest.model.HelpRequest;
import com.communityhelp.app.helprequest.repository.HelpRequestRepository;
import com.communityhelp.app.user.model.User;
import com.communityhelp.app.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;

    private final DonationRepository donationRepository;
    private final HelpRequestRepository helpRequestRepository;

    /**
     * Crea o recupera una conversación asociada a una entidad (Donation o HelpRequest).
     * 1. Busca o crea la conversación.
     * 2. Valida que el usuario tenga permiso para acceder a la entidad relacionada:
     * - DONATION - solo donor o volunteer asignado.
     * - HELP_REQUEST - solo requester o volunteer asignado.
     * - ADMIN - siempre permitido.
     * 3. Trae todos los participantes de la conversación desde la BD
     */
    @Override
    public ConversationResponseDto getOrCreateConversation(
            UUID relatedEntityId,
            String type,
            UUID userId,
            Authentication authentication
    ) {

        ConversationType conversationType = ConversationType.valueOf(type);

        validateUserHasAccess(relatedEntityId, conversationType, userId, authentication);

        if (conversationType == ConversationType.DONATION) {
            if (!donationRepository.existsById(relatedEntityId)) {
                throw new EntityNotFoundException("Donation not found");
            }
        } else if (conversationType == ConversationType.HELP_REQUEST) {
            if (!helpRequestRepository.existsById(relatedEntityId)) {
                throw new EntityNotFoundException("HelpRequest not found");
            }
        }

        Conversation conversation = conversationRepository
                .findByTypeAndRelatedEntityId(conversationType, relatedEntityId)
                .orElseGet(() -> {
                    Conversation newConversation = Conversation.builder()
                            .type(conversationType)
                            .relatedEntityId(relatedEntityId)
                            .build();
                    return conversationRepository.save(newConversation);
                });

        // Obtiene IDs de participantes según la entidad
        List<UUID> participantIds;
        if (conversationType == ConversationType.DONATION) {
            participantIds = donationRepository.findById(relatedEntityId)
                    .map(d -> {
                        List<UUID> ids = new ArrayList<>();
                        ids.add(d.getDonor().getId());
                        if (d.getVolunteer() != null) ids.add(d.getVolunteer().getId());
                        return ids;
                    })
                    .orElseThrow(() -> new EntityNotFoundException("Donation not found"));
        } else {
            participantIds = helpRequestRepository.findById(relatedEntityId)
                    .map(h -> {
                        List<UUID> ids = new ArrayList<>();
                        ids.add(h.getRequester().getId());
                        if (h.getVolunteer() != null) ids.add(h.getVolunteer().getId());
                        return ids;
                    })
                    .orElseThrow(() -> new EntityNotFoundException("HelpRequest not found"));
        }

        // Añade participantes que no existan aún en la conversación (evitando duplicados)
        for (UUID participantId : participantIds) {

            participantRepository
                    .findByConversation_IdAndUser_Id(conversation.getId(), participantId)
                    .orElseGet(() -> {

                        User user = userRepository.findById(participantId)
                                .orElseThrow(() -> new EntityNotFoundException("User not found"));

                        ConversationParticipant participant = ConversationParticipant.builder()
                                .conversation(conversation)
                                .user(user)
                                .joinedAt(LocalDateTime.now())
                                .build();

                        return participantRepository.save(participant);
                    });
        }

        ConversationResponseDto dto = conversationMapper.toDto(conversation);

        // Traer participantes desde la BD para asegurarnos que estén todos
        dto.setParticipants(
                participantRepository.findByConversation_Id(conversation.getId())
                        .stream()
                        .map(p -> ConversationResponseDto.ParticipantDto.builder()
                                .userId(p.getUser().getId())
                                .userName(p.getUser().getName())
                                .build()
                        )
                        .toList()
        );

        return dto;
    }

    /**
     * Obtiene todas las conversaciones en las que participa un usuario ordenadas por fecha.
     * - Usuario normal - Solo sus conversaciones.
     * - Admin - Todas las conversaciones del sistema.
     */
    @Override
    public Page<ConversationResponseDto> getUserConversations(
            UUID userId,
            int page,
            int size,
            Authentication authentication
    ) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());

        Page<Conversation> conversations;

        if (isAdmin(authentication)) {
            conversations = conversationRepository.findAll(pageable);
        } else {
            conversations = conversationRepository
                    .findDistinctByParticipants_User_Id(userId, pageable);
        }

        return conversations.map(conversation -> {

            ConversationResponseDto dto = conversationMapper.toDto(conversation);

            dto.setParticipants(
                    conversation.getParticipants().stream()
                            .map(p -> ConversationResponseDto.ParticipantDto.builder()
                                    .userId(p.getUser().getId())
                                    .userName(p.getUser().getName())
                                    .build()
                            ).toList()
            );

            return dto;
        });
    }

    /**
     * Envía un mensaje dentro de una conversación.
     * 1. Verifica que la conversación exista.
     * 2. Comprueba que el usuario sea participante.
     * 3. Crea el mensaje.
     * 4. Lo guarda y lo devuelve mapeado.
     */
    @Override
    public MessageResponseDto sendMessage(
            UUID conversationId,
            UUID senderId,
            MessageCreateRequestDto dto,
            Authentication authentication
    ) {

        if (isAdmin(authentication)) {
            throw new AccessDeniedException("Admin cannot send messages");
        }

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));

        participantRepository.findByConversation_IdAndUser_Id(conversationId, senderId)
                .orElseThrow(() -> new AuthorizationDeniedException("User not allowed in this conversation"));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(dto.getContent())
                .sentAt(LocalDateTime.now())
                .build();

        Message saved = messageRepository.save(message);

        // Actualiza updatedAt de la conversación
        conversation.touch();
        conversationRepository.save(conversation);

        return messageMapper.toDto(saved);
    }

    /**
     * Obtiene todos los mensajes de una conversación.
     * 1. Verifica que el usuario sea participante.
     * 2. Recupera los mensajes ordenados por fecha paginados.
     */
    @Override
    public Page<MessageResponseDto> getMessages(
            UUID conversationId,
            UUID userId,
            int page,
            int size,
            Authentication authentication
    ) {
        if (!isAdmin(authentication)) {
            participantRepository.findByConversation_IdAndUser_Id(conversationId, userId)
                    .orElseThrow(() -> new AccessDeniedException("User not allowed in this conversation"));
        }

        Pageable pageable = PageRequest.of(page, size);

        return messageRepository
                .findByConversation_IdOrderBySentAtAsc(conversationId, pageable)
                .map(messageMapper::toDto);
    }

    /**
     * Borra el mensaje de la conversación.
     * 1. Verifica que el mensaje perteneza a la conversación.
     * 2. Solo el autor del mensaje lo puede borrar (ventana de 15 minutos).
     * 3. El Admin puede borrar sin restricción de tiempo.
     * 4. Marca el mensaje como eliminado y registrando la fecha de la eliminación.
     */
    @Override
    public void deleteMessage(UUID conversationId, UUID messageId, UUID currentUserId, Authentication authentication) {

        boolean isAdmin = isAdmin(authentication);

        Message message = messageRepository
                .findByIdAndConversation_Id(messageId, conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        if (!isAdmin) {

            // Solo el autor puede borrar
            if (!message.getSender().getId().equals(currentUserId)) {
                throw new AuthorizationDeniedException("You can only delete your own messages");
            }

            // Ventana de 15 minutos
            LocalDateTime deletionLimit = message.getSentAt().plusMinutes(15);

            if (LocalDateTime.now().isAfter(deletionLimit)) {
                throw new IllegalStateException("Deletion time window expired");
            }
        }

        // Soft delete
        message.setDeleted(true);
        message.setDeletedAt(LocalDateTime.now());
        messageRepository.save(message);

        // Actualiza updatedAt de la conversación
        message.getConversation().touch();
        conversationRepository.save(message.getConversation());
    }

    /**
     * Valida que el usuario tenga acceso a la conversación asociada a una Donation o HelpRequest.
     * - DONATION → Solo donor o volunteer asignado.
     * - HELP_REQUEST → Solo requester o volunteer asignado.
     * - ADMIN → Siempre permitido.
     */
    private void validateUserHasAccess(
            UUID relatedEntityId,
            ConversationType type,
            UUID userId,
            Authentication authentication
    ) {

        if (isAdmin(authentication)) {
            return;
        }

        if (type == ConversationType.DONATION) {

            Donation donation = donationRepository.findById(relatedEntityId)
                    .orElseThrow(() -> new EntityNotFoundException("Donation not found"));

            boolean isDonor = donation.getDonor().getId().equals(userId);
            boolean isVolunteer = donation.getVolunteer() != null &&
                    donation.getVolunteer().getUser().getId().equals(userId);

            if (!isDonor && !isVolunteer) {
                throw new AccessDeniedException("You are not allowed to access this conversation");
            }

        } else if (type == ConversationType.HELP_REQUEST) {

            HelpRequest helpRequest = helpRequestRepository.findById(relatedEntityId)
                    .orElseThrow(() -> new EntityNotFoundException("HelpRequest not found"));

            boolean isRequester = helpRequest.getRequester().getId().equals(userId);
            boolean isVolunteer = helpRequest.getVolunteer() != null &&
                    helpRequest.getVolunteer().getUser().getId().equals(userId);

            if (!isRequester && !isVolunteer) {
                throw new AccessDeniedException("You are not allowed to access this conversation");
            }
        }
    }

    /**
     * Comprueba si el usuario autenticado tiene el rol de ADMIN.
     */
    private boolean isAdmin(Authentication authentication) {
        return authentication != null &&
                authentication.getAuthorities().stream()
                        .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

}
