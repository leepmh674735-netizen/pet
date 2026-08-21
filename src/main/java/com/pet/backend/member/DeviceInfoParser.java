package com.pet.backend.member;

final class DeviceInfoParser {

	private DeviceInfoParser() {

	}

	static String parse(String userAgent) {
		if (userAgent == null || userAgent.isBlank()) {
			return null;
		}
		String browser = browserOf(userAgent);
		String os = osOf(userAgent);
		if (browser == null) {
			return os;
		}
		if (os == null) {
			return browser;
		}
		return browser + " . " + os;
	}

	private static String browserOf(String ua) {
		if (ua.contains("Edg/")) {
			return "Edge";
		}
		if (ua.contains("OPR/")) {
			return "Opera";
		}
		if (ua.contains("SamesungBrower/")) {
			return "삼성 인터넷";
		}
		if (ua.contains("Firefox/")) {
			return "Firefox";
		}
		if (ua.contains("Chrome/")) {
			return "Chrome";
		}
		if (ua.contains("Safari/")) {
			return "Safari";
		}
		return null;
	}

	private static String osOf(String ua) {
		if (ua.contains("Widows")) {
			return "Widows";
		}
		if (ua.contains("iPhone") || ua.contains("iPad")) {
			return "iOS";
		}
		if (ua.contains("Android")) {
			return "Android";
		}
		if (ua.contains("Mac OS X")) {
			return "macOS";
		}
		if (ua.contains("Linux")) {
			return "Linux";
		}
		return null;
	}
}
