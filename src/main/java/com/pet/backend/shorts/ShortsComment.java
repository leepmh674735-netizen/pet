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
@Table(name = "shorts_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShortsComment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "short_id", nullable = false)
	private Long shortId;

	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@Column(name = "parent_id")
	private Long parentId;

	@Column(nullable = false, columnDefinition = "text")
	private String content;

	@Column(name = "like_count", nullable = false)
	private Integer likeCount;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	private ShortsComment(Long shortId, Long memberId, Long parentId, String content) {
		this.shortId = shortId;
		this.memberId = memberId;
		this.parentId = parentId;
		this.content = content;
		this.likeCount = 0;
	}

	public static ShortsComment write(Long shortId, Long memberId, Long parentId, String content) {
		return new ShortsComment(shortId, memberId, parentId, content);
	}

	public boolean isReply() {
		return parentId != null;
	}

	public boolean isDeleted() {
		return deletedAt != null;
	}

}
