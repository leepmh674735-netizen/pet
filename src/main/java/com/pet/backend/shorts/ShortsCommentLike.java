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
@Table(name = "shorts_comment_like")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShortsCommentLike {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "comment_id", nullable = false)
	private Long commentId;

	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@CreationTimestamp
	@Column(name = "created_at")
	private Instant createdAt;

	private ShortsCommentLike(Long commentId, Long memberId) {
		this.commentId = commentId;
		this.memberId = memberId;
	}

	public static ShortsCommentLike of(Long commententId, Long memberId) {
		return new ShortsCommentLike(commententId, memberId);
	}

}
