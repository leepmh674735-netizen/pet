package com.pet.backend.mcp;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.pet.backend.prediction.DiseasePrediction;
import com.pet.backend.prediction.DiseasePredictionClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DiseasePredictionMcpTool {

	private final DiseasePredictionClient diseasePredictionClient;
	private final WebLinks websLinks;

	@Tool(description = "반려동물 ID로 질병 예측 결과(예측 소견 심각도 근거)를 조회한다." + "실제 진단이 아니라 참고용 소견이므로 필요하면 병원상담을 권해야 한다.")
	public String getDiseasePrediction(@ToolParam(description = "반려동물 ID") Long petId) {
		DiseasePrediction prediction = diseasePredictionClient.predict(petId);
		return """
				예측 소견: %s
				심각도: %s
				근거: %s
				자세히 보기: %s
				""".formatted(prediction.prediction(), severityLabel(prediction.servertiry()), prediction.basis(),
				websLinks.diagnosisUrl());
	}

	private String severityLabel(String severity) {
		return switch (severity) {
		case "LOW" -> "낮음";
		case "MEDIUM" -> "보통";
		case "HIGH" -> "높음";
		default -> severity;
		};
	}
}
