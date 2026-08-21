package com.pet.backend.shorts;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShortsCommentRepository extends JpaRepository<ShortsComment, Long> {

	@Query("""
			select new com.pet.backend.shorts.ShortsCommentRow(
			  c.id, c.parentId, m.name, m.profileImageUrl, c.content, c.likeCount, c.createdAt)
			  from ShortsComment c
			  join com.pet.backend.member.Member m on m.id = c.memberId
			  where c.shortId = :shortsId
			  and c.deletedAt is null
			  order by c.id asc
			""")
	List<ShortsCommentRow> findRowsByShortsId(@Param("shortsId") Long shortsId);

	Optional<ShortsComment> findByIdAndDeletedAtIsNull(Long id);

	@Modifying(clearAutomatically = true)
	@Query("update ShortsComment c set c.likeCount = c.likeCount + 1 where c.id = :id")
	void increaseLikeCount(@Param("id") Long id);

	@Modifying(clearAutomatically = true)
	@Query("update ShortsComment c set c.likeCount = c.likeCount - 1 where c.id = :id and c.likeCount > 0")
	void decreaseLikeCount(@Param("id") Long id);

	@Query("select c.likeCount from ShortsComment c where c.id = :id")
	Integer findLikeCount(@Param("id") Long id);

}