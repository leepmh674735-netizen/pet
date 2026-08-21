package com.pet.backend.skin;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Component
public class SkinAiClient {
	
	private final RestClient restClient;
	
	public SkinAiClient(@Value("${ai.server.url}") String aiServerUrl) {
		this.restClient = RestClient.builder()
				.baseUrl(aiServerUrl)
				.build();
	}
	
	public SkinDiagnosisResultDto requestSkinDiagnosis(MultipartFile file) throws IOException {
		return sendPredictRequest(file, "/api/v1/predict");
	}
	
	public SkinDiagnosisResultDto requestBinarySkinDiagnosis(MultipartFile file) throws IOException {
		return sendPredictRequest(file, "api/v1/predict/binary");
	}
	
	private SkinDiagnosisResultDto sendPredictRequest(MultipartFile file, String uriPath) throws IOException {
		ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
			@Override
			public String getFilename() {
				return file.getOriginalFilename() != null ? file.getOriginalFilename() : "skin_image.jpg";
			}
		};
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType(
				file.getContentType() != null ? file.getContentType() : MediaType.IMAGE_JPEG_VALUE));
		
		HttpEntity<ByteArrayResource> fileEntity = new HttpEntity<>(fileResource, headers);
		
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", fileEntity);
		
		return restClient.post()
				.uri(uriPath)
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.body(body)
				.retrieve()
				.body(SkinDiagnosisResultDto.class);
		
	}
}
