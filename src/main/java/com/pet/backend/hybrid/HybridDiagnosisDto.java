package com.pet.backend.hybrid;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class HybridDiagnosisDto {

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Request {
		private Double age;
		private Double weight;
		private Double crp;
		private Double il6;

		@JsonProperty("text_prompt")
		@JsonAlias({ "text_prompt", "textPrmopt" })
		private String textPrompt;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Response {
		private boolean success;
		private String status;
		private String diagnosis;

		@JsonProperty("is_notmal")
		@JsonAlias({ "is_normal", "isNormal" })
		private boolean isNormal;

		private Double confidence;
		private Map<String, Double> probabilities;
		private String details;
	}

}
