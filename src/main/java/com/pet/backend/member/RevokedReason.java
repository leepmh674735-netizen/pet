package com.pet.backend.member;

public enum RevokedReason {
	ROTATED,
	LOGOUT,
	REUSE_DETECTED,
	PASSWORD_CHANGED,
	REPLACED_BY_LOGIN,
	DEVICE_REVOKED,
	WITHDRAWN;
	
	public boolean exemptFromReuseDetection() {
		return this == PASSWORD_CHANGED || this == REPLACED_BY_LOGIN
				|| this == DEVICE_REVOKED || this == WITHDRAWN;
	}

}
