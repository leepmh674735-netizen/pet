package com.pet.backend.chat;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "room_id")
	private Long id;

	@Column(name = "room_name", nullable = false, length = 100)
	private String name;

	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ChatCategory category;

	@Column(length = 200)
	private String description;

	@Column(name = "max_members")
	private Integer maxMembers;

	@Column(name = "pinned_message_id")
	private Long pinnedMessageId;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	private ChatRoom(String name, Long memberId, ChatCategory category, String description, Integer maxMembers) {
		this.name = name;
		this.memberId = memberId;
		this.category = category;
		this.description = description;
		this.maxMembers = maxMembers;
	}

	public static ChatRoom create(String name, Long memberId, ChatCategory category, String description,
			Integer maxMembers) {
		return new ChatRoom(name, memberId, category, description, maxMembers);
	}

	public void updateProfile(String name, ChatCategory category, String description, Integer maxMembers) {
		this.name = name;
		this.category = category;
		this.description = description;
		this.maxMembers = maxMembers;
	}

	public void pin(Long messageId) {
		this.pinnedMessageId = messageId;
	}

	public void unpin() {
		this.pinnedMessageId = null;
	}

	public boolean isDeleted() {
		return deletedAt != null;
	}

	public void delete() {
		this.deletedAt = Instant.now();
	}

}
