package com.pet.backend.mcp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WebLinks {
	
	private final String baseUrl;
	
	public WebLinks(@Value("${app.web-base-url:http://localhost:5173}") String baseUrl) {
		this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
	}
	
	public String mapUrl() {
		return baseUrl + "/map";
	}
	
	public String walkUrl()  {
		return baseUrl + "/walk";
	}
	
	public String diagnosisUrl() {
		return baseUrl + "/skin/diagnosis";
	}

}
