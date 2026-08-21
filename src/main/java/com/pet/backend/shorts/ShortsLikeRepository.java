package com.pet.backend.shorts;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShortsLikeRepository extends JpaRepository<ShortsLike, Long> {

	boolean existsByShortIdAndMemberId(Long shortId, Long memberId);
	
	long deleteByShortIdAndMemberId(Long shortId, Long memberId);
	
	@Query("select sl.shortId from ShortsLike sl where sl.memberId = :memberId and sl.shortId in :shortIds")
	List<Long> findLikedShortIds(
			@Param("memberId") Long memberId,
			@Param("shortIds") Collection<Long> ShortIds);
}
