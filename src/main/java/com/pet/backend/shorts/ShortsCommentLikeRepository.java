package com.pet.backend.shorts;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShortsCommentLikeRepository extends JpaRepository<ShortsCommentLike, Long>{

	boolean existsByCommentIdAndMemberId(Long commentId, Long memberId);
	
	long deleteByCommentIdAndMemberId(Long commentId, Long memberId);
	
	@Query("select cl.commentId from ShortsCommentLike cl "
			  + "where cl.memberId = :memberId and cl.commentId in :commentIds")
	
	List<Long> findLikedCommentIds(
			@Param("memberId") Long memberId,
			@Param("commentIds") Collection<Long> commentIds);
}
