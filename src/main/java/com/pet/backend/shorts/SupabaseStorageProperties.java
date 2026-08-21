package com.pet.backend.shorts;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "suabase")
public record SupabaseStorageProperties(
		String url,
		String serviceRoleKey,
		String shortsBucket
) {
	
	public boolean isConfigured() {
		return url != null && !url.isBlank()
				&& serviceRoleKey != null && !serviceRoleKey.isBlank();
	}

}
