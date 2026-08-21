package com.pet.backend.chat.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.pet.backend.chat.ChatMemberKickedEvent;
import com.pet.backend.chat.ChatMembersChangedEvent;
import com.pet.backend.chat.ChatMessageCreatedEvent;
import com.pet.backend.chat.dto.ChatPinChangedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatBroadcaster {

	private final SimpMessagingTemplate messageTemplate;
	private final ChatWebSocketSessionRegistry sessionRegistry;

	static String roomTopic(Long roomId) {
		return "/topic/chat/rooms/" + roomId;
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onMessageCreate(ChatMessageCreatedEvent event) {
		messageTemplate.convertAndSend(roomTopic(event.roomId()), ChatEvent.message(event.message()));
	}

	@TransactionalEventListener
	public void onMemberKicked(ChatMemberKickedEvent event) {
		sessionRegistry.disconnectMember(event.memberId());
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
	public void onMembersChange(ChatMembersChangedEvent event) {
		messageTemplate.convertAndSend(roomTopic(event.roomId()), ChatEvent.membersChanged());
	}

	@TransactionalEventListener
	public void onPinChange(ChatPinChangedEvent event) {
		messageTemplate.convertAndSend(roomTopic(event.roomId()), ChatEvent.pindChanged());
	}

}
