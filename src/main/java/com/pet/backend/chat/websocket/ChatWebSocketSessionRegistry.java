package com.pet.backend.chat.websocket;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ChatWebSocketSessionRegistry {

	private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
	private final Map<String, Long> sessionMembers = new ConcurrentHashMap<>();

	void register(WebSocketSession session) {
		sessions.put(session.getId(), session);
	}

	void binMember(String sessionId, Long memberId) {
		sessionMembers.put(sessionId, memberId);
	}

	void unregister(String sessionId) {
		sessions.remove(sessionId);
		sessionMembers.remove(sessionId);
	}

	Long findMemberId(String sessionId) {
		return sessionMembers.get(sessionId);
	}

	void disconnectMember(Long memberId) {
		sessionMembers.forEach((sessionId, ownerId) -> {
			if (!ownerId.equals(memberId)) {
				return;
			}
			WebSocketSession session = sessions.get(sessionId);
			if (session == null) {
				return;
			}
			try {
				session.close(CloseStatus.NORMAL);
			} catch (IOException e) {
				log.warn("강퇴 회원 세션 종료 sessionId={}", sessionId, e);
			}
		});
	}
}
