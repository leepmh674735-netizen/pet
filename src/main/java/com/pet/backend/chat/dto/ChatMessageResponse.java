package com.pet.backend.chat.dto;

import java.time.Instant;

import com.pet.backend.chat.ChatMessage;

public record ChatMessageResponse(
		Long id,
		Long senderId,
		String senderName,
		String senderProfileImageUrl,
		String content,
		Instant createdAt
) {
	
	public static ChatMessageResponse of(ChatMessage message, String senderName,
			String senderProfileImageUrl) {
		return new ChatMessageResponse(message.getId(), message.getSenderId(), senderName, 
				senderProfileImageUrl, message.getContent(), message.getCreatedAt());
	}

}
