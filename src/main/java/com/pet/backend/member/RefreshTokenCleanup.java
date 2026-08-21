package com.pet.backend.member;

import java.time.Duration;
import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenCleanup {

	static final Duration RETENTION_AFTER_EXPIRY = Duration.ofDays(7);

	private final RefreshTokenRepository refreshTokenRepository;

	@Scheduled(cron = "0 30 4 * * *")
	@Transactional
	public void purgeExpiredTokens() {
		int deleted = refreshTokenRepository
				.deleteAllExpiredBefore(Instant.now().minus(RETENTION_AFTER_EXPIRY));
		if (deleted > 0) {
			log.info("만료 리프레쉬 토큰 {}건 삭제 (만료 후 {}일 경과분)",
					deleted, RETENTION_AFTER_EXPIRY.toDays());
		}
	}

}
