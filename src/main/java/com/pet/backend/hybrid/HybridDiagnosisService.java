package com.pet.backend.hybrid;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HybridDiagnosisService {

	private final HybridAiClient hybridAiClient;

	public HybridDiagnosisDto.Response diagnoseHybridHealth(HybridDiagnosisDto.Request requestDto) {
		return hybridAiClient.requestHybridDiagnosis(requestDto);
	}
}
