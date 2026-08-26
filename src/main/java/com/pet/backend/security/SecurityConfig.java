package com.pet.backend.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final RestAuthenticationEntryPoint authenticationPoint;
	private final JwtTokenProvider jwtTokenProvider;
	private final ObjectMapper objectMapper;

	@Value("${app.cors.allowed-origins}")
	private List<String> allowedOrigins;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filter(HttpSecurity http) throws Exception {
		http.cors(cors -> cors.configurationSource(corsConfigurationSource())).csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.formLogin(AbstractHttpConfigurer::disable).httpBasic(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(auth -> auth

						.requestMatchers("/api/member/signup", "/api/members/login", "/api/members/login/kakao",
								"/api/members/refresh", "/api/members/logout", "/api/v1/skin/**", "/api/vi/hybrid/**")
						.permitAll()
						.requestMatchers("/", "/index.html", "/*.html", "/*.css", "/*.js", "/*.jpg", "/*.png", "/*.gif", "/*.svg", "/favicon.ico")
						.permitAll()

						.requestMatchers("/ws").permitAll()

						.requestMatchers(HttpMethod.GET, "/api/shorts").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/shorts/comments").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/ads").permitAll().anyRequest().authenticated())
				.exceptionHandling(handler -> handler.authenticationEntryPoint(authenticationPoint))
				.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, objectMapper),
						UsernamePasswordAuthenticationFilter.class);
		return http.build();

	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration confing = new CorsConfiguration();
		confing.setAllowedOrigins(allowedOrigins);
		confing.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		confing.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", confing);
		return source;
	}

}
