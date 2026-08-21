package com.pet.backend.member;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByTokenHash(String tokenHash);

	List<RefreshToken> findAllByMemberIdAndRevokedAtIsNull(Long memberId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update RefreshToken t set t.revokedAt = :now, t.revokedReason = :reason
			where t.memberId = :memberId and t.sessionId = :sessionId and t.revokedAt is null
			""")
	int revokeAllBySession(
			@Param("memberId") Long memberId,
			@Param("sessionId") UUID sessionId,
			@Param("now") Instant now,
			@Param("reason") RevokedReason reason);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update RefreshToken t set t.revokedReason = :reason
			where t.memberId = :memberId and t.sessionId = :sessionId
			and t.revokedReason = :rotated and t.revokedAt > :graceCutoff
			""")
	int expireRotationGraceBySession(
			@Param("memberId") Long memberId,
			@Param("sessionId") UUID sessionId,
			@Param("reason") RevokedReason reason,
			@Param("rotated") RevokedReason rotated,
			@Param("graceCutoff") Instant graceCutoff);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update RefreshToken t set t.revokedReason = :reason
			where t.memberId = :memberId
			and t.revokedReason = :rotated and t.revokedAt > :graceCutoff
			""")
	int expireRotationGraceByMember(
			@Param("memberId") Long memberId,
			@Param("reason") RevokedReason reason,
			@Param("rotated") RevokedReason rotated,
			@Param("graceCutoff") Instant graceCutoff);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update RefreshToken t set t.revokedAt = :now, t.revokedReason = :reason
			where t.memberId = :memberId and t.revokedAt is null
			""")
	int revokeAllByMemberId(
			@Param("memberId") Long memberId,
			@Param("now") Instant now,
			@Param("reason") RevokedReason reason);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("delete from RefreshToken t where t.expiresAt < :cutoff")
	int deleteAllExpiredBefore(@Param("cutoff") Instant cutoff);
}