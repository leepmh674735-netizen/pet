package com.pet.backend.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.WebMvcSseServerTransportProvider;

@SpringBootTest
class McpHttpDefaultProfileIsolationTest {

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	void 기본_프로파일에서는_MCP_서버_빈이_생성되지_않는다() {
		assertThat(applicationContext.getBeanNamesForType(McpSyncServer.class)).isEmpty();
		assertThat(applicationContext.getBeanNamesForType(WebMvcSseServerTransportProvider.class)).isEmpty();
	}

	@Test
	void 기본_프로파일에서는_MCP_HTTP_전용_보안체인이_생성되지_않는다() {

		assertThat(applicationContext.containsBean("mcpHttpSecurityFilterChain")).isFalse();
	}
}
