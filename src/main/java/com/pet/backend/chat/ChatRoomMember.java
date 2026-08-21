package com.pet.backend.chat;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_room_member")
@DynamicUpdate
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomMember {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "room_id", nullable = false)
	private Long roomId;

	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private ChatRole role;

	@CreationTimestamp
	@Column(name = "joined_at", nullable = false, updatable = false)
	private Instant joinedAt;

	@Column(name = "left_at")
	private Instant leftAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "left_reason", length = 10)
	private ChatLeftReason leftReason;

	@Column(name = "last_read_message_id")
	private Long lastReadMessageId;

	@Version
	@Column(nullable = false)
	private Long version;

	private ChatRoomMember(Long roomId, Long memberId, ChatRole role) {
		this.roomId = roomId;
		this.memberId = memberId;
		this.role = role;
	}

	public static ChatRoomMember owner(Long roomId, Long memberId) {
		return new ChatRoomMember(roomId, memberId, ChatRole.OWNER);
	}

	public static ChatRoomMember join(Long roomId, Long memberId, Long lastestMessageId) {
		ChatRoomMember member = new ChatRoomMember(roomId, memberId, ChatRole.MEMBER);
		member.lastReadMessageId = lastestMessageId;
		return member;
	}

	public void leave() {
		this.leftAt = Instant.now();
		this.leftReason = ChatLeftReason.LEFT;
	}

	public void kick() {
		this.leftAt = Instant.now();
		this.leftReason = ChatLeftReason.KICKED;
	}

	public void changeRole(ChatRole role) {
		this.role = role;
	}

}
