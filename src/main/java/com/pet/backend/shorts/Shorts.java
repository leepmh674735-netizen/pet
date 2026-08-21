package com.pet.backend.shorts;

import java.time.Instant;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
@Table(name = "shorts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Shorts {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@Column(name = "video_url", nullable = false, columnDefinition = "text")
	private String videoUrl;

	@Column(name = "thumbnail_url", columnDefinition = "text")
	private String thumbnailUrl;

	@Column(columnDefinition = "text")
	private String caption;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "json")
	private List<String> tags;

	@Column(name = "duration_sec", nullable = false)
	private Integer durationSec;

	@Column(name = "view_count", nullable = false)
	private Integer viewCount;

	@Column(name = "like_count", nullable = false)
	private Integer likeCount;

	@Column(name = "comment_count", nullable = false)
	private Integer commentCount;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	private Shorts(Long memberId, String videoUrl, String thumbnailUrl, String caption, List<String> tags,
			Integer durationSec) {
		this.memberId = memberId;
		this.videoUrl = videoUrl;
		this.thumbnailUrl = thumbnailUrl;
		this.caption = caption;

		this.tags = (tags == null || tags.isEmpty()) ? null : List.copyOf(tags);
		this.durationSec = durationSec;

		this.viewCount = 0;
		this.likeCount = 0;
		this.commentCount = 0;
	}

	public static Shorts upload(Long memberId, String videoUrl, String thumbnailUrl, String caption, List<String> tags,
			Integer durationSec) {
		return new Shorts(memberId, videoUrl, thumbnailUrl, caption, tags, durationSec);
	}

	public boolean isDeleted() {
		return deletedAt != null;
	}
}