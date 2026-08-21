package com.pet.backend.shorts;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shorts_like")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShortsLike {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "short_id", nullable = false)
	private Long shortId;

	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	private ShortsLike(Long shortId, Long memberId) {
		this.shortId = shortId;
		this.memberId = memberId;
	}

	public static ShortsLike of(Long shortId, Long memberId) {
		return new ShortsLike(shortId, memberId);
	}

}
