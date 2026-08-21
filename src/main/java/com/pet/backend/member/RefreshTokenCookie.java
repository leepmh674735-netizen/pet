package com.pet.backend.member;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
class RefreshTokenCookie {

	static final String NAME = "refreshToken";

	private static final String PATH = "/api/members";

	private final boolean secure;

	RefreshTokenCookie(@Value("${app.cookie.secure:false}") boolean secure) {
		this.secure = secure;
	}

	ResponseCookie create(String rawToken) {
		return base(rawToken).maxAge(RefreshTokenService.TOKEN_TTL).build();
	}

	ResponseCookie expire() {
		return base("")
				.maxAge(0)
				.build();
	}

	private ResponseCookie.ResponseCookieBuilder base(String value) {
		return ResponseCookie.from(NAME, value).httpOnly(true)
				.secure(secure)
				.sameSite("Lax")
				.path(PATH);
	}
}