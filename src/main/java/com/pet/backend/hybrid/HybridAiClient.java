package com.pet.backend.hybrid;

import org.apache.catalina.connector.RequestFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HybridAiClient {
	
	private final RestClient restClient;
	
	public HybridAiClient(@Value("${ai.server.url:http://localhost:8000}") String aiServerUrl) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(3000);
		requestFactory.setReadTimeout(3000);
		
		restClient = RestClient.builder()
				.requestFactory(requestFactory)
				.baseUrl(aiServerUrl)
				.build();
	}
	
	public HybridDiagnosisDto.Response requestHybridDiagnosis(HybridDiagnosisDto.Request requestDto) {
		return restClient.post()
				.uri("/api/v1/predict/hybrid")
				.contentType(MediaType.APPLICATION_JSON)
				.body(requestDto)
				.retrieve()
				.body(HybridDiagnosisDto.Response.class);
	}

}
