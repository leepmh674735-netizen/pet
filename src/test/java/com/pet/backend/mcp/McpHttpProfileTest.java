package com.pet.backend.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import io.modelcontextprotocol.server.transport.WebMvcSseServerTransportProvider;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("mcp-http")
class McpHttpProfileTest {

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	void mcp_http_프로파일에서는_SSE_전송_빈이_등록된다() {
		assertThat(applicationContext.getBeanNamesForType(WebMvcSseServerTransportProvider.class)).isNotEmpty();

	}

	@Test
	void mcp_http_프로파일에서는_MCP_전용_보안체인이_등록된다() {
		assertThat(applicationContext.containsBean("mcpHttpSecurityFilterChain")).isTrue();
	}

	@Test
	void mcp_http_프로파일에서는_기존_도구_빈은_그대로_공유된다() {

		assertThat(applicationContext.getBeanNamesForType(DiseasePredictionMcpTool.class)).hasSize(1);
		assertThat(applicationContext.getBeanNamesForType(PlaceSearchMcpTool.class)).hasSize(1);
		assertThat(applicationContext.getBeanNamesForType(WalkWeatherMcpTool.class)).hasSize(1);
		assertThat(applicationContext.getBeanNamesForType(WalkBriefingMcpTool.class)).hasSize(1);
	}

}
