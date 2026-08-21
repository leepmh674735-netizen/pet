package com.pet.backend.shorts;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShortsRepository extends JpaRepository<Shorts, Long> {

	@Query(value = """
			select s.id
			from shorts s
			join pet_member m on m.id = s.member_id
			where s.deleted_at is null
			  and m.deleted_at is null
			  and s.id not in (:excludeIds)
			order by ( ln(1 + s.like_count::double precision) * 1.0
			         + ln(1 + s.comment_count::double precision) * 1.5 )
			         / power((extract(epoch from (now() - s.created_at)) / 3600 + 2)::double precision, 1.2)
			         desc,
			         s.id desc
			limit :limit
			""", nativeQuery = true)
	List<Long> findRankedIds(@Param("excludeIds") Collection<Long> excludeIds, @Param("limit") int limit);

	@Query(value = """
			with tag_affinity as (
			    select t.tag,
			           sum(
			               case e.type
			                   when 'like' then 2
			                   when 'comment' then 4
			                   when 'share' then 5
			                   when 'skip' then -3
			                   else 0
			               end
			               + coalesce(ln(1 + least(coalesce(e.watch_ms, 0), es.duration_sec * 1000 * 3)
			                                 ::double precision
			                             / nullif(es.duration_sec * 1000, 0)) * 6, 0)
			           ) as aff
			    from shorts_event e
			    join shorts es on es.id = e.short_id
			    cross join lateral unnest(es.tags) as t(tag)
			    where e.member_id = :memberId
			      and e.created_at > now() - interval '30 days'
			    group by t.tag
			)
			select s.id
			from shorts s
			join pet_member m on m.id = s.member_id
			where s.deleted_at is null
			  and m.deleted_at is null
			  and s.member_id <> :memberId
			  and s.id not in (:excludeIds)
			order by
			    case when exists (
			        select 1 from shorts_event v
			        where v.member_id = :memberId and v.short_id = s.id and v.type = 'view'
			    ) then 1 else 0 end,
			    ( (ln(1 + s.like_count::double precision) * 1.0
			     + ln(1 + s.comment_count::double precision) * 1.5)
			      / power((extract(epoch from (now() - s.created_at)) / 3600 + 2)::double precision, 1.2) )
			    * least(greatest(1 + coalesce(
			          (select sum(a.aff) from tag_affinity a where a.tag = any(s.tags)), 0) * 0.05,
			        0.2), 3.0)
			    desc,
			    s.id desc
			limit :limit
			""", nativeQuery = true)
	List<Long> findPersonalizedRankedIds(
			@Param("memberId") Long memberId,
			@Param("excludeIds") Collection<Long> excludeIds, 
			@Param("limit") int limit
	);

	@Query("""
			select new com.pet.backend.shorts.ShortsResponse(
			    s.id, m.name, s.videoUrl, s.thumbnailUrl, s.caption, s.tags,
			    s.durationSec, s.viewCount, s.likeCount, s.commentCount, s.createdAt)
			from Shorts s
			join com.pet.backend.member.Member m on m.id = s.memberId
			where s.id in :ids
			  and s.deletedAt is null
			  and m.deletedAt is null
			""")
	List<ShortsResponse> findAllByIds(@Param("ids") Collection<Long> ids);

	Optional<Shorts> findByIdAndDeletedAtIsNull(Long id);

	boolean existsByIdAndDeletedAtIsNull(Long id);

	@Modifying(clearAutomatically = true)
	@Query("update Shorts s set s.likeCount = s.likeCount + 1 where s.id = :id")
	void increaseLikeCount(@Param("id") Long id);

	@Modifying(clearAutomatically = true)
	@Query("update Shorts s set s.likeCount = s.likeCount - 1 where s.id = :id and s.likeCount > 0")
	void decreaseLikeCount(@Param("id") Long id);

	@Modifying(clearAutomatically = true)
	@Query("update Shorts s set s.commentCount = s.commentCount + 1 where s.id = :id")
	void increaseCommentCount(@Param("id") Long id);

	@Query("select s.likeCount from Shorts s where s.id = :id")
	Integer findLikeCount(@Param("id") Long id);
}