package com.pet.backend.mcp;

import java.lang.reflect.Method;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class McpToolConfig {

	private final DiseasePredictionMcpTool diseasePredictionMcpTool;
	private final PlaceSearchMcpTool placeSearchMcpTool;
	private final WalkWeatherMcpTool walkWeatherMcpTool;
	private final WalkBriefingMcpTool walkBriefingMcpTool;

	@Bean
	public ToolCallbackProvider petCareMcpTools() {
		return MethodToolCallbackProvider.builder()
				.toolObjects(diseasePredictionMcpTool, placeSearchMcpTool, walkWeatherMcpTool, walkBriefingMcpTool)
				.build();
	}

}
