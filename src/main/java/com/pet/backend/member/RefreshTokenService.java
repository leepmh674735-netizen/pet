package com.pet.backend.member;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pet.backend.common.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

	public static final Duration TOKEN_TTL = Duration.ofDays(14);
	static final Duration ROTATION_GRACE = Duration.ofSeconds(30);

	private static final SecureRandom RANDOM = new SecureRandom();

	private final RefreshTokenRepository refreshTokenRepository;
	private final RefreshTokenReuseHandler reuseHandler;

	static Instant rotationGraceCutoff() {
		return Instant.now().minus(ROTATION_GRACE);
	}

	@Transactional
	public String issue(Long memberId, String deviceInfo) {
		return persistNewToken(memberId, UUID.randomUUID(), deviceInfo, Instant.now());
	}

	@Transactional
	public String rotate(RefreshToken token) {
		if (!token.isRevoked()) {
			token.revoke(RevokedReason.ROTATED);
		}
		return persistNewToken(
				token.getMemberId(),
				token.getSessionId(),
				token.getDeviceInfo(),
				token.getSessionStartedAt());
	}

	@Transactional
	public String reissueAfterPasswordChange(Long memberId, String deviceInfo) {
		refreshTokenRepository.revokeAllByMemberId(memberId, Instant.now(), RevokedReason.PASSWORD_CHANGED);
		return persistNewToken(memberId, UUID.randomUUID(), deviceInfo, Instant.now());
	}

	private String persistNewToken(Long memberId, UUID sessionId, String deviceInfo, Instant sessionStartedAt) {
		String rawToken = generateToken();
		refreshTokenRepository.save(RefreshToken.issue(memberId, hash(rawToken), Instant.now().plus(TOKEN_TTL),
				sessionId, deviceInfo, sessionStartedAt));
		return rawToken;
	}

	@Transactional
	public void revoke(String rawToken) {
		revokeIfActive(rawToken, RevokedReason.LOGOUT);
	}

	@Transactional
	public void revokeAllOnWithdraw(Long memberId) {
		refreshTokenRepository.revokeAllByMemberId(memberId, Instant.now(), RevokedReason.WITHDRAWN);
	}

	@Transactional
	public void revokeReplaceByLogin(String rawToken) {
		revokeIfActive(rawToken, RevokedReason.REPLACED_BY_LOGIN);
	}

	private void revokeIfActive(String rawToken, RevokedReason reason) {
		if (rawToken == null || rawToken.isBlank()) {
			return;
		}
		refreshTokenRepository.findByTokenHash(hash(rawToken)).filter(token -> !token.isRevoked())
				.ifPresent(token -> token.revoke(reason));
	}

	@Transactional(readOnly = true)
	public UUID findSessionIdOf(String rawToken) {
		if (rawToken == null || rawToken.isBlank()) {
			return null;
		}
		return refreshTokenRepository.findByTokenHash(hash(rawToken)).map(RefreshToken::getSessionId).orElse(null);
	}

	@Transactional(readOnly = true)
	public List<RefreshToken> findActiveTokens(Long memberId) {
		return refreshTokenRepository.findAllByMemberIdAndRevokedAtIsNull(memberId);
	}

	@Transactional
	public int revokeSession(Long memberId, UUID sessionId) {
		int revoked = refreshTokenRepository.revokeAllBySession(memberId, sessionId, Instant.now(),
				RevokedReason.DEVICE_REVOKED);
		int graceExpired = refreshTokenRepository.expireRotationGraceBySession(memberId, sessionId,
				RevokedReason.DEVICE_REVOKED, RevokedReason.ROTATED, rotationGraceCutoff());
		return revoked + graceExpired;
	}

	RefreshToken findUsableOrThrow(String rawToken) {
		if (rawToken == null || rawToken.isBlank()) {
			throw new BusinessException(MemberErrorCode.INVALID_REFRESH_TOKEN);
		}

		RefreshToken token = refreshTokenRepository.findByTokenHash(hash(rawToken))
				.orElseThrow(() -> new BusinessException(MemberErrorCode.INVALID_REFRESH_TOKEN));

		if (token.isExpired()) {
			throw new BusinessException(MemberErrorCode.REFRESH_EXPIRED);
		}

		if (token.isRevoked() && !token.isWithinRotationGrace(ROTATION_GRACE)) {
			if (token.getRevokedReason() != null && token.getRevokedReason().exemptFromReuseDetection()) {
				throw new BusinessException(MemberErrorCode.INVALID_REFRESH_TOKEN);
			}

			reuseHandler.revokeAllOf(token.getMemberId());
			throw new BusinessException(MemberErrorCode.INVALID_REFRESH_TOKEN);
		}

		return token;
	}

	private String generateToken() {
		byte[] bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	String hash(String rawToken) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", e);
		}
	}
}