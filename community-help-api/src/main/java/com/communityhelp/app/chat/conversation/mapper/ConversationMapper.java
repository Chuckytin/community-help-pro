package com.communityhelp.app.chat.conversation.mapper;

import com.communityhelp.app.chat.conversation.dto.ConversationResponseDto;
import com.communityhelp.app.chat.conversation.model.Conversation;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper para convertir Conversation en ConversationResponseDto.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ConversationMapper {

    ConversationResponseDto toDto(Conversation conversation);
}
