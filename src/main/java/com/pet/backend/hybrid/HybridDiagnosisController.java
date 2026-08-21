package com.pet.backend.hybrid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/hybrid")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HybridDiagnosisController {

	private final HybridDiagnosisService hybridDiagnosisService;

	@PostMapping("/diagnosis")
	public ResponseEntity<HybridDiagnosisDto.Response> diagnoseHybridHealth(
			@RequestBody HybridDiagnosisDto.Request requestDto) {
		HybridDiagnosisDto.Response response = hybridDiagnosisService.diagnoseHybridHealth(requestDto);
		return ResponseEntity.ok(response);
	}
}
