package com.devotedmc.ExilePearl.util;

public enum Permission
{
	PLAYER("player"),
	CHECK("check"),
	DECAY("decay"),
	EXILE_ANY("exileany"),
	FREE_ANY("freeany"),
	LIST("list"),
	RELOAD("reload"),
	SET_HEALTH("sethealth"),
	CONFIG("config")
	;
	
	/**
	 * The node string that is referenced for permissions
	 */
	public final String node;
	
	Permission(final String node) {
		this.node = "exilepearl." + node;
	}
}
