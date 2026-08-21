package com.pet.backend.chat.dto;

import java.time.Instant;

import com.pet.backend.chat.ChatCategory;
import com.pet.backend.chat.ChatRoom;

public record ChatRoomResponse(
		Long id,
		String name,
		ChatCategory category,
		String description,
		long participantCount,
		Integer maxMembers,
		Long ureadCount,
		Instant createdAt
) {
	
	public static ChatRoomResponse of(ChatRoom room, long participantCount, Long unreadCount) {
		return new ChatRoomResponse(
				room.getId(), 
				room.getName(), 
				room.getCategory(), 
				room.getDescription(), 
				participantCount, 
				room.getMaxMembers(),
				unreadCount, 
				room.getCreatedAt());
	}

}
