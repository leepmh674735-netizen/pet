package com.pet.backend.chat;

import com.pet.backend.chat.dto.ChatMessageResponse;

public record ChatMessageCreatedEvent(Long roomId, ChatMessageResponse message) {

}
