package com.pet.backend.skin;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/skin/diagnosis")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SkinDiagnosisController {

	private final SkinDiagnosisService skinDiagnosisService;

	@PostMapping
	public ResponseEntity<SkinDiagnosisResultDto> dianoseSkin(@RequestParam("file") MultipartFile file)
			throws IOException {
		SkinDiagnosisResultDto result = skinDiagnosisService.diagnoseSkinDisease(file);
		return ResponseEntity.ok(result);
	}

	@PostMapping("/binary")
	public ResponseEntity<SkinDiagnosisResultDto> diagnoseBinarySkin(@RequestParam("file") MultipartFile file)
			throws IOException {
		SkinDiagnosisResultDto result = skinDiagnosisService.diagnoseBinarySkinDisease(file);
		return ResponseEntity.ok(result);
	}

}
