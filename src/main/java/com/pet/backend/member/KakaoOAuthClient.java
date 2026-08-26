package com.pet.backend.member;

import java.time.Duration;

import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.databind.JsonNode;
import com.pet.backend.common.BusinessException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KakaoOAuthClient {

	record KakaoUserInfo(String providerId, String email, String nickname) {
	}

	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
	private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

	private final KakaoOAuthProperties properties;
	private final RestClient authClient;
	private final RestClient apiClient;

	KakaoOAuthClient(KakaoOAuthProperties properties) {
		this.properties = properties;
		this.authClient = clientFor("https://kauth.kakao.com");
		this.apiClient = clientFor("https://kapi.kakao.com");
	}

	private static RestClient clientFor(String baseUrl) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
		requestFactory.setReadTimeout(READ_TIMEOUT);
		return RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
	}

	KakaoUserInfo fetchUser(String code, String redirectUrl) {
		return fetchUserInfo(exchangeCode(code, redirectUrl));
	}

	private String exchangeCode(String code, String redirectUrl) {
		if (properties.clientId() == null || properties.clientId().isBlank()) {

			log.error("KAKAO OAUTH CLIENT_ID가 비어 있어 카카오 로그인을 수행할 수 없습니다 -.env 확인");
			throw new BusinessException(MemberErrorCode.SOCIAL_LOGIN_FAILED);
		}
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("grant_type", "authorization_code");
		form.add("client_id", properties.clientId());
		form.add("redirect_uri", redirectUrl);
		form.add("code", code);
		if (properties.clientSecret() != null && !properties.clientSecret().isBlank()) {
			form.add("client_secret", properties.clientSecret());
		}
		try {
			JsonNode body = authClient.post().uri("/oauth/token").contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.body(form).retrieve().body(JsonNode.class);
			String accessToken = body == null ? null : body.path("access_token").asText(null);
			if (accessToken == null) {
				log.warn("카카오 토큰 응답에 access_token이 없습니다.");
				throw new BusinessException(MemberErrorCode.SESSION_NOT_FOUND);
			}
			return accessToken;
		} catch (RestClientException e) {

			log.warn("카카오 토큰 교환 실패: {}", e.getMessage());
			throw new BusinessException(MemberErrorCode.SOCIAL_LOGIN_FAILED);
		}
	}

	private KakaoUserInfo fetchUserInfo(String kakaoAccessToken) {
		try {
			JsonNode body = apiClient.get().uri("/v2/user/me").header("Authorization", "Bearer " + kakaoAccessToken)
					.retrieve().body(JsonNode.class);
			String providerId = body == null ? null : body.path("id").asText(null);
			if (providerId == null) {
				log.warn("카카오 사용자 정보 응답에 id가 없습니다.");
				throw new BusinessException(MemberErrorCode.SOCIAL_LOGIN_FAILED);
			}
			JsonNode account = body.path("kakao_accout");
			return new KakaoUserInfo(providerId, account.path("email").asText(null),
					account.path("profile").path("nickname").asText(null));
		} catch (RestClientException e) {
			log.warn("카카오 사용자 정보 조회 실패: {}", e.getMessage());
			throw new BusinessException(MemberErrorCode.SOCIAL_LOGIN_FAILED);
		}
	}

}
