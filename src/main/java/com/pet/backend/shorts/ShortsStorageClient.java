package com.pet.backend.shorts;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.pet.backend.common.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShortsStorageClient {

	private final SupabaseStorageProperties properties;
	private final RestClient.Builder restClientBuilder;

	public String upload(String path, byte[] bytes, String mimeType) {
		if (!properties.isConfigured()) {
			throw new BusinessException(ShortsErrorCode.UPLOAD_FAILED,
					"서버에 Storage 설정이 없습니다. .env의 SUPABASE_URL / SUPABASE_SERVICE_ROLE_KEY를 확인해 주세요.");
		}

		String baseUrl = trimTrailingSlash(properties.url());
		String objectUrl = "%s/storage/v1/object/%s/%s".formatted(baseUrl, properties.shortsBucket(), path);

		try {
			restClientBuilder.build().post().uri(objectUrl)

					.header("apikey", properties.serviceRoleKey())
					.header("Authorization", "Bearer " + properties.serviceRoleKey())
					.contentType(MediaType.parseMediaType(mimeType))

					.body(bytes).retrieve().toBodilessEntity();
		} catch (RestClientException e) {

			log.error("Supabase Storage 업로드 실패 path={}", path, e);
			throw new BusinessException(ShortsErrorCode.UPLOAD_FAILED);
		}

		return "%s/storage/v1/object/public/%s/%s".formatted(baseUrl, properties.shortsBucket(), path);
	}

	private String trimTrailingSlash(String url) {
		return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
	}

}
