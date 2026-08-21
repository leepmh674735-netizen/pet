package com.pet.backend.common;

import java.time.Duration;
import java.util.Set;

import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ImageStorageClient {

	private static final long MAX_IMAGE_BYTES = 5 * 1024 * 1024;
	private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
	private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

	private final ImageStorageProperties properties;
	private final RestClient restClient;

	ImageStorageClient(ImageStorageProperties properties, RestClient.Builder restClientBuilder) {
		this.properties = properties;
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
		requestFactory.setReadTimeout(READ_TIMEOUT);
		this.restClient = restClientBuilder.requestFactory(requestFactory).build();
	}

	public void validateImage(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BusinessException(CommonErrorCode.VALIDATION_ERROR, "이미지 파일은 필수입니다.");
		}
		if (file.getContentType() == null || !ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
			throw new BusinessException(CommonErrorCode.VALIDATION_ERROR, "jpeg.png.webp 이미지만 업로드할 수 있습니다.");
		}
		if (file.getSize() > MAX_IMAGE_BYTES) {
			throw new BusinessException(CommonErrorCode.VALIDATION_ERROR, "이미지는 5MB 이하여야 합니다.");
		}
	}

	public String upload(String path, byte[] bytes, String mimeType) {
		if (!properties.isConfigured()) {
			throw new BusinessException(CommonErrorCode.IMAGE_UPLOAD_FAILED,
					"서버에 Storag 설정이 없습니다. .env의 SUPABASE_URL / SUPABASE_SERVICE_ROLE_KEY를 확인해 주세요.");
		}

		String baseUrl = trimTrailingSlash(properties.url());
		String objectUrl = "%s/storage/v1/object/%s/%s".formatted(baseUrl, properties.profilesBucket(), path);

		try {
			restClient.post().uri(objectUrl).header("apikey", properties.serviceRoleKey())
					.header("Authorization", "Bearer " + properties.serviceRoleKey()).header("x-upsert", "true")
					.contentType(MediaType.parseMediaType(mimeType)).body(bytes).retrieve().toBodilessEntity();
		} catch (RestClientException e) {

			log.error("프로필 이미지 업로드 실패 path={}", path, e);
			throw new BusinessException(CommonErrorCode.IMAGE_UPLOAD_FAILED);
		}

		return "%s/storage/v1/object/public/%s/%s".formatted(baseUrl, properties.profilesBucket(), path);
	}

	private String trimTrailingSlash(String url) {
		return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
	}
}
