package com.pet.backend.shorts;

import java.time.Instant;
import java.util.List;

public record ShortsResponse(
		Long id,
		String memberName,
		String videoUrl,
		String thumbnailUrl,
		String caption,
		List<String> tags,
		Integer durationSec,
		Integer viewCount,
		Integer likeCount,
		Integer commentCount,
		Instant createdAt,
		boolean likedByMe
) {

	public ShortsResponse(Long id, String memberName, String videoUrl, String thumbnailUrl, String caption,
			List<String> tags, Integer durationSec, Integer viewCount, Integer likeCount, Integer commentCount,
			Instant createdAt) {
		this(id, memberName, videoUrl, thumbnailUrl, caption, tags, durationSec, viewCount, likeCount, commentCount,
				createdAt, false);
	}

	public ShortsResponse withLikedByMe(boolean liked) {
		return new ShortsResponse(id, memberName, videoUrl, thumbnailUrl, caption, tags, durationSec, viewCount,
				likeCount, commentCount, createdAt, liked);
	}

	public static ShortsResponse of(Shorts shorts, String memberName) {
		return new ShortsResponse(
				shorts.getId(),
				memberName,
				shorts.getVideoUrl(),
				shorts.getThumbnailUrl(),
				shorts.getCaption(),
				shorts.getTags(),
				shorts.getDurationSec(),
				shorts.getViewCount(),
				shorts.getLikeCount(),
				shorts.getCommentCount(),
				shorts.getCreatedAt()
		);
	}
}