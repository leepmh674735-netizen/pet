package com.pet.backend.member;

import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenReuseHandler {

	private final RefreshTokenRepository refreshTokenRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	void revokeAllOf(Long memberId) {
		int revoked = refreshTokenRepository.revokeAllByMemberId(
				memberId, Instant.now(), RevokedReason.REUSE_DETECTED);
		
		int graceExpired = refreshTokenRepository.expireRotationGraceByMember(
				memberId, RevokedReason.REUSE_DETECTED,
				RevokedReason.ROTATED, RefreshTokenService.rotationGraceCutoff());
				log.warn("리프레쉬 토큰 재 사용 감지 - memberId= {}, 폐기한 활성 토큰 {}개 + 유예 토큰 {}개",
						memberId, revoked, graceExpired);
	}
}
