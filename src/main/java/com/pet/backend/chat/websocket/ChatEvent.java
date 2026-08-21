package com.pet.backend.chat.websocket;

import com.pet.backend.chat.dto.ChatMessageResponse;

public record ChatEvent<T>(String type, T data) {

	public static ChatEvent<ChatMessageResponse> message(ChatMessageResponse message) {
		return new ChatEvent<>("MESSAGE", message);
	}

	public static ChatEvent<Void> membersChanged() {
		return new ChatEvent<>("MEMBERS_CHANGED", null);
	}

	public static ChatEvent<Void> pindChanged() {
		return new ChatEvent<>("PIN_CHANGED", null);
	}
}
