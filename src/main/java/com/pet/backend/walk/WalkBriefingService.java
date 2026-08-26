package com.pet.backend.walk;

import com.pet.backend.common.BusinessException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalkBriefingService {

	private static final ZoneId KST = ZoneId.of("Asia/Seoul");
	private static final int GAP_GOOD_MIN_DAYS = 2;

	private final WalkRecordRepository walkRecordRepository;
	private final WalkBriefingRepository walkBriefingRepository;
	private final KmaClient kmaClient;

	@Transactional
	public void runBriefing() {
		Instant now = Instant.now();
		Optional<WalkRecord> lastRecord = walkRecordRepository.findFirstByOrderByStartedAtDesc();

		if (lastRecord.isEmpty()) {
			walkBriefingRepository.save(WalkBriefing.skipNoRecord(now, "마지막 산책 기록이 없어 판정을 건너뜁니다."));
			log.info("산책 브리핑: 최근 산책 기록이 없어 skip_no_record로 저장했습니다.");
			return;
		}

		WalkRecord record = lastRecord.get();
		GeoPoint origin = firstPointOf(record);
		if (origin == null) {
			walkBriefingRepository.save(WalkBriefing.skipNoRecord(now, "마지막 산책 기록에 경로 좌표가 없어 판정을 건너뜁니다."));
			log.warn("산책 브리핑: walk_record id={}에 path가 비어 있어 skip 처리했습니다.", record.getId());
			return;
		}

		int gapDays = (int) Duration.between(record.getStartedAt(), now).toDays();

		WeatherResult weather;
		try {
			weather = fetchWeather(origin.lat(), origin.lng());
		} catch (BusinessException e) {
			log.warn("산책 브리핑: 날씨 조회 실패로 이번 회차는 저장하지 않습니다.", e);
			return;
		}

		Judgement judgement = judge(weather, gapDays);

		walkBriefingRepository.save(WalkBriefing.judged(now, origin.lat(), origin.lng(), weather.airTemp(),
				weather.windSpeed(), weather.humidity(), weather.solar(), weather.asphaltTemp(), weather.riskLevel(),
				weather.precipitation(), gapDays, record.getPetId(), judgement.event(), judgement.shouldNotify(),
				judgement.reason()));

		log.info("산책 브리핑 저장 완료 — event={}, notify={}, gapDays={}", judgement.event().code(), judgement.shouldNotify(),
				gapDays);
	}

	@Transactional(readOnly = true)
	public Optional<WalkBriefingSummary> getTodaysBriefing() {
		Instant now = Instant.now();
		Instant startOfDay = LocalDate.now(KST).atStartOfDay(KST).toInstant();
		return walkBriefingRepository.findFirstByCheckedAtBetweenOrderByCheckedAtDesc(startOfDay, now)
				.map(WalkBriefingSummary::from);
	}

	private Judgement judge(WeatherResult weather, int gapDays) {
		boolean isHot = weather.riskLevel() == RiskLevel.DANGER || weather.riskLevel() == RiskLevel.SEVERE;
		if (isHot) {
			return new Judgement(WalkBriefingEvent.HOT, true,
					"아스팔트 온도 약 %.1f℃로 위험 단계입니다.".formatted(weather.asphaltTemp()));
		}
		if (!weather.precipitation() && gapDays >= GAP_GOOD_MIN_DAYS) {
			return new Judgement(WalkBriefingEvent.GAP_GOOD, true,
					"산책 공백 %d일 이상 + 강수 없음으로 산책하기 좋은 조건입니다.".formatted(gapDays));
		}
		return new Judgement(WalkBriefingEvent.NONE, false, "알림 조건 미충족 (아스팔트 약 %.1f℃, 공백 %d일, 강수 %s)"
				.formatted(weather.asphaltTemp(), gapDays, weather.precipitation() ? "있음" : "없음"));
	}

	private GeoPoint firstPointOf(WalkRecord record) {
		List<GeoPoint> path = record.getPath();
		return (path == null || path.isEmpty()) ? null : path.get(0);
	}

	private WeatherResult fetchWeather(double lat, double lng) {
		KmaGridConverter.Grid grid = KmaGridConverter.toGrid(lat, lng);
		KmaWeatherSnapshot snapshot = kmaClient.fetch(grid.nx(), grid.ny());

		double solar = SolarEstimator.estimate(lat, lng, ZonedDateTime.now(KST), snapshot.sky(), snapshot.pty());
		double asphaltTemp = AsphaltTempCalculator.calculate(snapshot.airTemp(), snapshot.windSpeed(), solar);
		RiskLevel riskLevel = RiskLevel.from(asphaltTemp);
		boolean precipitation = snapshot.pty() != 0;

		return new WeatherResult(snapshot.airTemp(), snapshot.humidity(), snapshot.windSpeed(), solar, asphaltTemp,
				riskLevel, precipitation);
	}

	private record WeatherResult(double airTemp, double humidity, double windSpeed, double solar, double asphaltTemp,
			RiskLevel riskLevel, boolean precipitation) {
	}

	private record Judgement(WalkBriefingEvent event, boolean shouldNotify, String reason) {
	}
}