package com.pet.backend.walk;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

record KmaBaseTime(String baseDate, String baseTime) {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final int NCST_PUBLISH_MINUTE = 40;
	private static final int FCST_PUBLISH_MINUTE = 45;

	static KmaBaseTime forUltraSrtNcst(ZonedDateTime now) {
		return of(now, NCST_PUBLISH_MINUTE);
	}

	static KmaBaseTime forUltraSrtFcst(ZonedDateTime now) {
		return of(now, FCST_PUBLISH_MINUTE);
	}

	private static KmaBaseTime of(ZonedDateTime now, int publishMinute) {
		ZonedDateTime base = now.getMinute() < publishMinute ? now.minusHours(1) : now;
		String date = base.format(DATE_FORMAT);
		String time = String.format("%02d%02d", base.getHour(), publishMinute);
		return new KmaBaseTime(date, time);
	}
}
