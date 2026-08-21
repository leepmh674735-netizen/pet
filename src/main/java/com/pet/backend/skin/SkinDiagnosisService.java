package com.pet.backend.skin;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SkinDiagnosisService {

	private final SkinAiClient skinAiClient;

	public SkinDiagnosisResultDto diagnoseSkinDisease(MultipartFile file) throws IOException {
		return skinAiClient.requestSkinDiagnosis(file);
	}

	public SkinDiagnosisResultDto diagnoseBinarySkinDisease(MultipartFile file) throws IOException {
		return skinAiClient.requestBinarySkinDiagnosis(file);
	}
}
