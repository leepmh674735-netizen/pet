package com.pet.backend.mcp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("mcp-http")
public class McpHttpSecurityConfig {

	@Bean
	@Order
	public SecurityFilterChain mcpHttpSecurityFilterChain(HttpSecurity http) throws Exception {
		http

				.securityMatcher("/sse/**", "/mcp/**").csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
		return http.build();
	}
}
