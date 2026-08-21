package com.pet.backend.member;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "token_id")
	private Long id;

	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@Column(name = "token_hash", nullable = false, length = 64)
	private String tokenHash;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "session_id", nullable = false)
	private UUID sessionId;

	@Column(name = "device_info", length = 100)
	private String deviceInfo;

	@Column(name = "session_started_at")
	private Instant sessionStartedAt;

	@Column(name = "revoked_at")
	private Instant revokedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "revoked_reason", length = 20)
	private RevokedReason revokedReason;

	private RefreshToken(Long memberId, String tokenHash, Instant expiresAt, UUID sessionId, String deviceInfo,
			Instant sessionStartedAt) {
		this.memberId = memberId;
		this.tokenHash = tokenHash;
		this.expiresAt = expiresAt;
		this.sessionId = sessionId;
		this.deviceInfo = deviceInfo;
		this.sessionStartedAt = sessionStartedAt;
	}

	public static RefreshToken issue(Long memberId, String tokenHash, Instant expiresAt, UUID sessionId,
			String deviceInfo, Instant sessionStartedAt) {
		return new RefreshToken(memberId, tokenHash, expiresAt, sessionId, deviceInfo, sessionStartedAt);
	}

	public void revoke(RevokedReason reason) {
		if (revokedAt != null) {
			return;
		}
		this.revokedAt = Instant.now();
		this.revokedReason = reason;
	}

	public boolean isRevoked() {
		return revokedAt != null;
	}

	public boolean isWithinRotationGrace(Duration grace) {
		return revokedReason == RevokedReason.ROTATED && revokedAt != null
				&& revokedAt.isAfter(Instant.now().minus(grace));
	}

	public boolean isExpired() {
		return expiresAt.isBefore(Instant.now());
	}

}
