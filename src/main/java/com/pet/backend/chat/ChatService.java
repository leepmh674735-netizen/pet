package com.pet.backend.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pet.backend.chat.dto.ChatMemberResponse;
import com.pet.backend.chat.dto.ChatMessageCreateRequest;
import com.pet.backend.chat.dto.ChatMessageResponse;
import com.pet.backend.chat.dto.ChatPinChangedEvent;
import com.pet.backend.chat.dto.ChatRoomResponse;
import com.pet.backend.chat.dto.ChatRoomSaveRequest;
import com.pet.backend.common.BusinessException;
import com.pet.backend.common.CommonErrorCode;
import com.pet.backend.member.Member;
import com.pet.backend.member.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomMemberRepository chatRoomMemberRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final MemberRepository memberRepository;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public ChatRoomResponse createRoom(Long memberId, ChatRoomSaveRequest request) {
		ChatRoom room = ChatRoom.create(
				request.name().trim(),
				memberId,
				request.category(),
				normalizeDescription(request.description()),
				request.maxMembers()
		);
		chatRoomRepository.save(room);
		chatRoomMemberRepository.save(ChatRoomMember.owner(room.getId(), memberId));
		return ChatRoomResponse.of(room, 1L, 0L);
	}

	@Transactional
	public ChatRoomResponse updateRoom(Long actorId, Long roomId, ChatRoomSaveRequest request) {
		ChatRoom room = getActiveRoom(roomId);
		requireOwner(roomId, actorId);
		room.updateProfile(
				request.name().trim(),
				request.category(),
				normalizeDescription(request.description()),
				request.maxMembers()
		);
		return ChatRoomResponse.of(room, countActive(roomId), null);
	}

	@Transactional(readOnly = true)
	public List<ChatRoomResponse> getRooms(Long memberId, String keyword, String category, String sort) {
		ChatCategory categoryFilter = parseCategory(category);
		boolean popular = parsePopularSort(sort);
		String keywordFilter = (keyword == null || keyword.isBlank()) ? "" : keyword.trim();
		List<ChatRoom> rooms = chatRoomRepository.searchActive(keywordFilter, categoryFilter);
		if (rooms.isEmpty()) {
			return List.of();
		}

		List<Long> roomIds = rooms.stream().map(ChatRoom::getId).toList();
		Map<Long, Long> counts = chatRoomMemberRepository.countActiveByRoomIds(roomIds).stream()
				.collect(Collectors.toMap(
						ChatRoomMemberRepository.RoomParticipantCount::getRoomId,
						ChatRoomMemberRepository.RoomParticipantCount::getParticipantCount
				));

		Map<Long, Long> unreads = chatRoomMemberRepository.countUnreadByMember(memberId).stream()
				.collect(Collectors.toMap(
						ChatRoomMemberRepository.RoomUnreadCount::getRoomId,
						ChatRoomMemberRepository.RoomUnreadCount::getUnreadCount
				));

		if (popular) {
			rooms = rooms.stream()
					.sorted(Comparator.comparingLong((ChatRoom room) -> counts.getOrDefault(room.getId(), 0L)).reversed())
					.toList();
		}
		return rooms.stream()
				.map(room -> ChatRoomResponse.of(room, counts.getOrDefault(room.getId(), 0L), unreads.get(room.getId())))
				.toList();
	}

	private ChatCategory parseCategory(String category) {
		if (category == null || category.isBlank()) {
			return null;
		}
		try {
			return ChatCategory.valueOf(category);
		} catch (IllegalArgumentException e) {
			throw new BusinessException(CommonErrorCode.VALIDATION_ERROR,
					"category는 WALK, TRAINING, HEALTH, FREE 중 하나여야 합니다.");
		}
	}

	private boolean parsePopularSort(String sort) {
		if (sort == null || sort.isBlank() || sort.equals("recent")) {
			return false;
		}
		if (sort.equals("popular")) {
			return true;
		}
		throw new BusinessException(CommonErrorCode.VALIDATION_ERROR, "sort는 recent 또는 popular여야 합니다.");
	}

	@Transactional
	public void markRead(Long memberId, Long roomId, Long lastReadMessageId) {
		getActiveRoom(roomId);
		chatRoomMemberRepository.markRead(roomId, memberId, lastReadMessageId);
	}

	@Transactional
	public ChatRoomResponse join(Long memberId, Long roomId) {
		ChatRoom room = getActiveRoom(roomId);
		if (chatRoomMemberRepository.existsByRoomIdAndMemberIdAndLeftReason(roomId, memberId, ChatLeftReason.KICKED)) {
			throw new BusinessException(ChatErrorCode.KICKED);
		}

		boolean inserted = false;
		if (!chatRoomMemberRepository.existsByRoomIdAndMemberIdAndLeftAtIsNull(roomId, memberId)) {
			if (room.getMaxMembers() != null && countActive(roomId) >= room.getMaxMembers()) {
				throw new BusinessException(ChatErrorCode.ROOM_FULL);
			}
			try {
				Long lastMessageId = chatMessageRepository.findTopByRoomIdOrderByIdDesc(roomId)
						.map(ChatMessage::getId)
						.orElse(null);
				chatRoomMemberRepository.save(ChatRoomMember.join(roomId, memberId, lastMessageId));
				inserted = true;
			} catch (DataIntegrityViolationException e) {
				if (!chatRoomMemberRepository.existsByRoomIdAndMemberIdAndLeftAtIsNull(roomId, memberId)) {
					throw e;
				}
			}
		}

		if (inserted) {
			revertIfJoinLost(room, memberId);
			eventPublisher.publishEvent(new ChatMembersChangedEvent(roomId));
		}

		return ChatRoomResponse.of(room, countActive(roomId), 0L);
	}

	private void revertIfJoinLost(ChatRoom room, Long memberId) {
		boolean kickedRace = chatRoomMemberRepository.existsByRoomIdAndMemberIdAndLeftReason(room.getId(), memberId,
				ChatLeftReason.KICKED);
		boolean overCapacity = room.getMaxMembers() != null && countActive(room.getId()) > room.getMaxMembers();
		if (!kickedRace && !overCapacity) {
			return;
		}
		chatRoomMemberRepository.findByRoomIdAndMemberIdAndLeftAtIsNull(room.getId(), memberId).ifPresent(joined -> {
			joined.leave();
			chatRoomMemberRepository.save(joined);
		});
		throw new BusinessException(kickedRace ? ChatErrorCode.KICKED : ChatErrorCode.ROOM_FULL);
	}

	private long countActive(Long roomId) {
		return chatRoomMemberRepository.countActiveByRoomIds(List.of(roomId)).stream()
				.mapToLong(ChatRoomMemberRepository.RoomParticipantCount::getParticipantCount)
				.findFirst()
				.orElse(0L);
	}

	@Transactional
	public ChatMessageResponse sendMessage(Long memberId, Long roomId, ChatMessageCreateRequest request) {
		getActiveRoom(roomId);
		requireParticipant(roomId, memberId);
		Member sender = memberRepository.findById(memberId).orElse(null);
		ChatMessage message = ChatMessage.of(roomId, memberId, request.content().trim());
		chatMessageRepository.save(message);
		ChatMessageResponse response = ChatMessageResponse.of(
				message,
				sender != null ? sender.getName() : "알 수 없음",
				sender != null ? sender.getProfileImageUrl() : null
		);
		eventPublisher.publishEvent(new ChatMessageCreatedEvent(roomId, response));
		return response;
	}

	@Transactional(readOnly = true)
	public List<ChatMessageResponse> getMessages(Long memberId, Long roomId, Long afterId, Long beforeId) {
		if (afterId != null && beforeId != null) {
			throw new BusinessException(CommonErrorCode.VALIDATION_ERROR, "afterId와 beforeId는 함께 사용할 수 없습니다.");
		}
		getActiveRoom(roomId);
		requireParticipant(roomId, memberId);

		List<ChatMessage> messages;
		if (afterId != null) {
			messages = chatMessageRepository.findTop500ByRoomIdAndIdGreaterThanOrderByIdAsc(roomId, afterId);
		} else if (beforeId != null) {
			messages = new ArrayList<>(chatMessageRepository.findTop50ByRoomIdAndIdLessThanOrderByIdDesc(roomId, beforeId));
			Collections.reverse(messages);
		} else {
			messages = new ArrayList<>(chatMessageRepository.findTop50ByRoomIdOrderByIdDesc(roomId));
			Collections.reverse(messages);
		}
		if (messages.isEmpty()) {
			return List.of();
		}

		Set<Long> senderIds = messages.stream().map(ChatMessage::getSenderId).collect(Collectors.toSet());
		Map<Long, Member> senders = memberRepository.findAllById(senderIds).stream()
				.collect(Collectors.toMap(Member::getId, member -> member));

		return messages.stream().map(message -> {
			Member sender = senders.get(message.getSenderId());
			return ChatMessageResponse.of(
					message,
					sender != null ? sender.getName() : "알 수 없음",
					sender != null ? sender.getProfileImageUrl() : null
			);
		}).toList();
	}

	@Transactional(readOnly = true)
	public List<ChatMemberResponse> getRoomMembers(Long memberId, Long roomId) {
		getActiveRoom(roomId);
		requireParticipant(roomId, memberId);
		List<ChatRoomMember> members = chatRoomMemberRepository.findByRoomIdAndLeftAtIsNullOrderByJoinedAtAsc(roomId);
		Set<Long> memberIds = members.stream().map(ChatRoomMember::getMemberId).collect(Collectors.toSet());
		Map<Long, Member> memberById = memberRepository.findAllById(memberIds).stream()
				.collect(Collectors.toMap(Member::getId, member -> member));

		return members.stream()
				.sorted(Comparator.comparingInt(member -> member.getRole().ordinal()))
				.map(member -> {
					Member found = memberById.get(member.getMemberId());
					return new ChatMemberResponse(
							member.getMemberId(),
							found != null ? found.getName() : "알 수 없음",
							member.getRole(),
							found != null ? found.getProfileImageUrl() : null
					);
				}).toList();
	}

	@Transactional
	public void leave(Long memberId, Long roomId) {
		getActiveRoom(roomId);
		ChatRoomMember me = chatRoomMemberRepository.findByRoomIdAndMemberIdAndLeftAtIsNull(roomId, memberId)
				.orElseThrow(() -> new BusinessException(ChatErrorCode.NOT_PARTICIPANT));
		if (me.getRole() == ChatRole.OWNER) {
			throw new BusinessException(ChatErrorCode.OWNER_CANNOT_LEAVE);
		}
		me.leave();
		eventPublisher.publishEvent(new ChatMembersChangedEvent(roomId));
	}

	@Transactional
	public void kick(Long actorId, Long roomId, Long targetMemberId) {
		getActiveRoom(roomId);
		ChatRoomMember actor = chatRoomMemberRepository.findByRoomIdAndMemberIdAndLeftAtIsNull(roomId, actorId)
				.orElseThrow(() -> new BusinessException(ChatErrorCode.NOT_PARTICIPANT));
		if (actorId.equals(targetMemberId)) {
			throw new BusinessException(ChatErrorCode.ROLE_FORBIDDEN);
		}
		ChatRoomMember target = getActiveMember(roomId, targetMemberId);
		if (!canKick(actor.getRole(), target.getRole())) {
			throw new BusinessException(ChatErrorCode.ROLE_FORBIDDEN);
		}
		target.kick();
		eventPublisher.publishEvent(new ChatMemberKickedEvent(roomId, targetMemberId));
		eventPublisher.publishEvent(new ChatMembersChangedEvent(roomId));
	}

	@Transactional
	public void changeRole(Long actorId, Long roomId, Long targetMemberId, ChatRole newRole) {
		getActiveRoom(roomId);
		requireOwner(roomId, actorId);
		if (newRole == ChatRole.OWNER) {
			throw new BusinessException(CommonErrorCode.VALIDATION_ERROR, "OWNER로의 변경은 위임 API(/delegate)를 사용해야 합니다.");
		}
		if (actorId.equals(targetMemberId)) {
			throw new BusinessException(CommonErrorCode.VALIDATION_ERROR, "자기 자신의 역할은 변경할 수 없습니다.");
		}
		getActiveMember(roomId, targetMemberId).changeRole(newRole);
		eventPublisher.publishEvent(new ChatMembersChangedEvent(roomId));
	}

	@Transactional
	public void delegate(Long actorId, Long roomId, Long targetMemberId) {
		getActiveRoom(roomId);
		ChatRoomMember actor = requireOwner(roomId, actorId);
		if (actorId.equals(targetMemberId)) {
			throw new BusinessException(CommonErrorCode.VALIDATION_ERROR, "자기 자신에게는 위임할 수 없습니다.");
		}
		ChatRoomMember target = getActiveMember(roomId, targetMemberId);
		actor.changeRole(ChatRole.MANAGER);
		chatRoomMemberRepository.saveAndFlush(actor);
		target.changeRole(ChatRole.OWNER);
		eventPublisher.publishEvent(new ChatMembersChangedEvent(roomId));
	}

	@Transactional
	public void deleteRoom(Long actorId, Long roomId) {
		ChatRoom room = getActiveRoom(roomId);
		requireOwner(roomId, actorId);
		room.delete();
	}

	@Transactional
	public void pinMessage(Long actorId, Long roomId, Long messageId) {
		ChatRoom room = getActiveRoom(roomId);
		requireOwnerOrManager(roomId, actorId);
		chatMessageRepository.findByIdAndRoomId(messageId, roomId)
				.orElseThrow(() -> new BusinessException(ChatErrorCode.MESSAGE_NOT_FOUND));
		room.pin(messageId);
		eventPublisher.publishEvent(new ChatPinChangedEvent(roomId));
	}

	@Transactional
	public void unpinMessage(Long actorId, Long roomId) {
		ChatRoom room = getActiveRoom(roomId);
		requireOwnerOrManager(roomId, actorId);
		if (room.getPinnedMessageId() == null) {
			return;
		}
		room.unpin();
		eventPublisher.publishEvent(new ChatPinChangedEvent(roomId));
	}

	@Transactional(readOnly = true)
	public ChatMessageResponse getPinnedMessage(Long memberId, Long roomId) {
		ChatRoom room = getActiveRoom(roomId);
		requireParticipant(roomId, memberId);
		if (room.getPinnedMessageId() == null) {
			return null;
		}
		ChatMessage message = chatMessageRepository.findById(room.getPinnedMessageId()).orElse(null);
		if (message == null) {
			return null;
		}
		Member sender = memberRepository.findById(message.getSenderId()).orElse(null);
		return ChatMessageResponse.of(
				message,
				sender != null ? sender.getName() : "알 수 없음",
				sender != null ? sender.getProfileImageUrl() : null
		);
	}

	private boolean canKick(ChatRole actor, ChatRole target) {
		return switch (actor) {
			case OWNER -> target != ChatRole.OWNER;
			case MANAGER -> target == ChatRole.MEMBER;
			case MEMBER -> false;
		};
	}

	private ChatRoomMember requireOwner(Long roomId, Long memberId) {
		return chatRoomMemberRepository.findByRoomIdAndMemberIdAndLeftAtIsNull(roomId, memberId)
				.filter(member -> member.getRole() == ChatRole.OWNER)
				.orElseThrow(() -> new BusinessException(ChatErrorCode.ROLE_FORBIDDEN));
	}

	private ChatRoomMember requireOwnerOrManager(Long roomId, Long memberId) {
		return chatRoomMemberRepository.findByRoomIdAndMemberIdAndLeftAtIsNull(roomId, memberId)
				.filter(member -> member.getRole() != ChatRole.MEMBER)
				.orElseThrow(() -> new BusinessException(ChatErrorCode.ROLE_FORBIDDEN));
	}

	private ChatRoomMember getActiveMember(Long roomId, Long memberId) {
		return chatRoomMemberRepository.findByRoomIdAndMemberIdAndLeftAtIsNull(roomId, memberId)
				.orElseThrow(() -> new BusinessException(ChatErrorCode.MEMBER_NOT_FOUND));
	}

	private String normalizeDescription(String description) {
		return (description == null || description.isBlank()) ? null : description.trim();
	}

	private ChatRoom getActiveRoom(Long roomId) {
		return chatRoomRepository.findById(roomId).filter(room -> !room.isDeleted())
				.orElseThrow(() -> new BusinessException(ChatErrorCode.ROOM_NOT_FOUND));
	}

	private void requireParticipant(Long roomId, Long memberId) {
		if (!chatRoomMemberRepository.existsByRoomIdAndMemberIdAndLeftAtIsNull(roomId, memberId)) {
			throw new BusinessException(ChatErrorCode.NOT_PARTICIPANT);
		}
	}
}