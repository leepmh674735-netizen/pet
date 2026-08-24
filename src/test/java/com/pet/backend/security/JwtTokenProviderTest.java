package com.pet.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import com.pet.backend.member.Role;

class JwtTokenProviderTest {

	private static final String VALID_SECRT = Base64.getEncoder().encodeToString(new byte[48]).replace('=', 'A');

	private JwtTokenProvider provider(String secret) {
		return new JwtTokenProvider(new JwtProperties(secret, 900_000L));
	}

	@Test
	void 짧은_시크릿은_기동_시점에_거부된다() {

		String tooShort = "a".repeat(63);
		assertThatThrownBy(() -> provider(tooShort)).isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("64").hasMessageContaining("63");
	}

	@Test
	void 유효한_시크릿은_64바이트다() {
		assertThat(VALID_SECRT.getBytes(StandardCharsets.UTF_8)).hasSize(64);
		assertThat(provider(VALID_SECRT)).isNotNull();
	}

	@Test
	void 발급한_토큰의_알고리즘은_HS512다() {
		String token = provider(VALID_SECRT).createAccessToken(1L, Role.MEBER);
		String header = new String(Base64.getUrlDecoder().decode(token.split("\\.")[0]), StandardCharsets.UTF_8);
		assertThat(header).contains("\"alg\":\"HS512\"");
	}

	@Test
	void 발급한_토큰을_그대로_파싱한다() {
		JwtTokenProvider provider = provider(VALID_SECRT);
		String token = provider.createAccessToken(42L, Role.MEBER);
		JwtTokenProvider.TokenPayload payload = provider.parse(token);
		assertThat(payload.memberId()).isEqualTo(42L);
		assertThat(payload.role()).isEqualTo(Role.MEBER);
	}
}
