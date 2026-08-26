package com.pet.backend.walk;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalkBriefingScheduler {

	private final WalkBriefingService walkBriefingService;

	@Scheduled(cron = "${walk.briefing.cron: 0 55 17 * * *}", zone = "Asia/Seoul")
	public void run() {
		try {
			walkBriefingService.runBriefing();
		} catch (Exception e) {
			log.error("산책 브리핑 스케줄 실행 중 예외가 발생했습니다.", e);
		}
	}
}
