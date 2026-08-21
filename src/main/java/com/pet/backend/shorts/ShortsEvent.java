package com.pet.backend.shorts;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shorts_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShortsEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "member_id")
	private Long memberId;

	@Column(name = "short_id", nullable = false)
	private Long shortsId;

	@Convert(converter = ShortsEventType.DbConverter.class)
	@Column(nullable = false, columnDefinition = "text")
	private ShortsEventType type;

	@Column(name = "watch_ms")
	private Integer watchMs;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	private ShortsEvent(Long memberId, Long shortId, ShortsEventType type, Integer watchMs) {
		this.memberId = memberId;
		this.shortsId = shortsId;
		this.type = type;
		this.watchMs = watchMs;
	}
	
	public static ShortsEvent watching(Long memberId, Long shortId, ShortsEventType type, Integer watchMs) {
		return new ShortsEvent(memberId, shortId, type, watchMs);
	}

	static ShortsEvent interaction(Long memberId, Long shortId, ShortsEventType type) {
		return new ShortsEvent(memberId, shortId, type, null);
	}
}
