package com.pet.backend.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "supabase")
public record ImageStorageProperties(
		String url,
		String serviceRoleKey,
		String profilesBucket
) {
	
	public boolean isConfigured() {
		return url != null && !url.isBlank()
				&& serviceRoleKey != null && !serviceRoleKey.isBlank();
	}

}
