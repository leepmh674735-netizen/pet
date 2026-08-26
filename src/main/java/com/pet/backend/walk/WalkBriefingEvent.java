package com.pet.backend.walk;

enum WalkBriefingEvent {

	HOT("hot"), GAP_GOOD("gap_good"), NONE("none"), SKIP_NO_RECORD("skip_no_record");

	private final String code;

	WalkBriefingEvent(String code) {
		this.code = code;
	}

	String code() {
		return code;
	}

	static WalkBriefingEvent fromCode(String code) {
		for (WalkBriefingEvent value : values()) {
			if (value.code.equals(code)) {
				return value;
			}
		}
		throw new IllegalArgumentException("알 수 없는 walk_briefing.event 값: " + code);
	}
}
