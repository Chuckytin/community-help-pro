package com.communityhelp.app.chat.message.mapper;

import com.communityhelp.app.chat.message.dto.MessageResponseDto;
import com.communityhelp.app.chat.message.model.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper para convertir Message en MessageResponseDto.
 * IGNORE - Se ignoran campos no mapeados.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MessageMapper {

    @Mapping(target = "conversationId", source = "conversation.id")
    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "senderName", expression = "java(resolveSenderName(message))")
    @Mapping(target = "content", expression = "java(resolveContent(message))")
    MessageResponseDto toDto(Message message);

    default String resolveSenderName(Message message) {
        if (message.getSender() == null) {
            return null;
        }

        if (!message.getSender().isActive()) {
            return "Deleted user";
        }

        return message.getSender().getName();
    }

    default String resolveContent(Message message) {
        if (message.isDeleted()) {
            return "Message deleted";
        }
        return message.getContent();
    }

}
