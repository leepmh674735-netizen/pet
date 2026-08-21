package com.pet.backend.place;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.pet.backend.common.BusinessException;

import com.pet.backend.security.SecurityConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
class KakaoClient {

	private final SecurityConfig securityConfig;

	private static final String KAKAO_LOCAL_BASE_URL = "https://dapi.kakao.com";
	private static final int SEARCH_RADIUS_METERS = 5_000;
	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
	private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

	private final RestClient restClient;
	private final boolean apiKeyConfigured;

	public KakaoClient(@Value("${kakao.rest-api-key}") String restApiKey, SecurityConfig securityConfig) {
		this.apiKeyConfigured = restApiKey != null && !restApiKey.isBlank();
		if (!apiKeyConfigured) {
			log.warn("kakao.rest-api-key(KAKAO_REST_API_KEY)가 설정되지 않았습니다. "
					+ "장소 검색 API 호출 시 예외가 발생합니다 -.env의 KAKAO_REST_API_KEY를 확인하세요.");
		}

		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
		requestFactory.setReadTimeout(READ_TIMEOUT);

		this.restClient = RestClient.builder().baseUrl(KAKAO_LOCAL_BASE_URL)
				.defaultHeader("Authorization", "KakaoAK" + restApiKey).build();

		this.securityConfig = securityConfig;
	}

	public KakaoSearchResponse searchKeyword(String query, double lat, double lng, String categoryGroupCode) {
		if (!apiKeyConfigured) {

			throw new IllegalStateException("KAKAKO_REST_API_KEY가 설정되지 않아 카카오 로컬 API를 호출할 수 없습니다.");
		}
		 try {
	            return restClient.get()
	                    .uri(uriBuilder -> {
	                        uriBuilder.path("/v2/local/search/keyword.json")
	                                .queryParam("query", query)
	                                .queryParam("x", lng)
	                                .queryParam("y", lat)
	                                .queryParam("radius", SEARCH_RADIUS_METERS);
	                        if (categoryGroupCode != null && !categoryGroupCode.isBlank()) {
	                            uriBuilder.queryParam("category_group_code", categoryGroupCode);
	                        }
	                        return uriBuilder.build();
	                    })
	                    .retrieve()
	                    .body(KakaoSearchResponse.class);
	        } catch (RestClientException e) {
	            log.warn("카카오 로컬 API 호출 실패: query={}", query, e);
	            throw new BusinessException(PlaceErrorCode.SEARCH_FAILED);
	        }
	    }
	}
