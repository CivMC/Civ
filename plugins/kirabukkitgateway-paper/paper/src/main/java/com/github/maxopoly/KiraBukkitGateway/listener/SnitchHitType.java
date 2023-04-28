package com.github.maxopoly.KiraBukkitGateway.listener;

public enum SnitchHitType {

	ENTER, LOGIN, LOGOUT;

	public static SnitchHitType fromString(String value) {
		return SnitchHitType.valueOf(value.trim().toUpperCase());
	}

}
