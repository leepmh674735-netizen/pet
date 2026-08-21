package com.pet.backend.shorts;

import java.time.Instant;
import java.util.List;

public record ShortsCommentResponse(
		Long id,
		String memberName,
		String memberProfileImageUrl,
		String conent,
		Integer likeCount,
		boolean likedByMe,
		Instant createdAt,
		List<ShortsCommentResponse> replies
) {
	
	public static ShortsCommentResponse of(ShortsCommentRow row, boolean likedByMe,
			List<ShortsCommentResponse> replies) {
		return new ShortsCommentResponse(row.id(), row.memberName(), row.memberProfileImagUrl(),
				row.content(), row.likeCount(), likedByMe, row.createdAt(), replies);
	}

}
