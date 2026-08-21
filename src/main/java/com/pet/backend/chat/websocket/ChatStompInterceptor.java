package com.pet.backend.chat.websocket;

import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import com.pet.backend.chat.ChatErrorCode;
import com.pet.backend.chat.ChatRoomMemberRepository;
import com.pet.backend.common.CommonErrorCode;
import com.pet.backend.common.ErrorCode;
import com.pet.backend.member.MemberErrorCode;
import com.pet.backend.security.JwtTokenProvider;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatStompInterceptor implements ChannelInterceptor {

	private static final String BEARER_PREFIX = "Bearer ";
	private static final Pattern ROOM_TOPIC = Pattern.compile("^/topic/chat/rooms/(//d+)$");

	private final JwtTokenProvider jwtTokenProvider;
	private final ChatRoomMemberRepository chatRoomMemberRepository;
	private final ChatWebSocketSessionRegistry sessionRegistry;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (accessor == null || accessor.getCommand() == null) {
			return message;
		}

		switch (accessor.getCommand()) {
		case CONNECT, STOMP -> authenticate(message, accessor);
		case SUBSCRIBE -> authorizeSubscribe(message, accessor);
		case UNSUBSCRIBE, DISCONNECT -> {
		}
		default -> throw reject(message, CommonErrorCode.FORBIDDEN);
		}
		return message;
	}

	private void authenticate(Message<?> message, StompHeaderAccessor accessor) {
		String header = accessor.getFirstNativeHeader("Authorization");
		if (header == null || !header.startsWith(BEARER_PREFIX)) {
			throw reject(message, MemberErrorCode.TOKEN_INVALID);
		}

		JwtTokenProvider.TokenPayload payload;
		try {
			payload = jwtTokenProvider.parse(header.substring(BEARER_PREFIX.length()));
		} catch (ExpiredJwtException e) {
			throw reject(message, MemberErrorCode.TOKEN_EXPIRED);
		} catch (JwtException | IllegalArgumentException e) {
			throw reject(message, MemberErrorCode.TOKEN_INVALID);
		}

		accessor.setUser(new ChatPrincipal(payload.memberId()));
		sessionRegistry.binMember(accessor.getSessionId(), payload.memberId());
	}

	private void authorizeSubscribe(Message<?> message, StompHeaderAccessor accessor) {
		String destination = accessor.getDestination();
		Matcher matcher = destination == null ? null : ROOM_TOPIC.matcher(destination);
		if (matcher == null || !matcher.matches()) {
			throw reject(message, CommonErrorCode.FORBIDDEN);
		}

		Long memberId = memberIdOf(accessor);
		if (memberId == null) {
			throw reject(message, MemberErrorCode.TOKEN_INVALID);
		}

		Long roomId = Long.valueOf(matcher.group(1));
		if (!chatRoomMemberRepository.existsByRoomIdAndMemberIdAndLeftAtIsNull(roomId, memberId))
			throw reject(message, ChatErrorCode.NOT_PARTICIPANT);
	}

	private Long memberIdOf(StompHeaderAccessor accessor) {
		Principal user = accessor.getUser();
		if (user instanceof ChatPrincipal principal) {
			return principal.memberId();
		}
		return sessionRegistry.findMemberId(accessor.getSessionId());
	}

	private MessageDeliveryException reject(Message<?> message, ErrorCode code) {
		return new MessageDeliveryException(message, code.getCode());
	}

	record ChatPrincipal(Long memberId) implements Principal {
		@Override
		public String getName() {
			return String.valueOf(memberId);
		}
	}
}